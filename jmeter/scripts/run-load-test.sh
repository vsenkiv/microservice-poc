#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Order Service Load Testing Script ===${NC}"

# Configuration
ORDER_SERVICE_HOST=${ORDER_SERVICE_HOST:-"order-service"}
ORDER_SERVICE_PORT=${ORDER_SERVICE_PORT:-"8081"}
RESULTS_DIR="/results"
TEST_PLAN="/test-plans/order-service-load-test.jmx"

# Test scenarios
declare -A SCENARIOS=(
    ["light_load"]="10 30 300"     # 10 users, 30s ramp-up, 5min duration
    ["medium_load"]="50 60 600"    # 50 users, 1min ramp-up, 10min duration
    ["heavy_load"]="100 120 900"   # 100 users, 2min ramp-up, 15min duration
    ["stress_test"]="200 180 600"  # 200 users, 3min ramp-up, 10min duration
)

# Function to run test
run_test() {
    local test_name=$1
    local threads=$2
    local ramp_up=$3
    local duration=$4
    local thread_type=$5

    echo -e "${YELLOW}Running ${test_name} with ${thread_type} threads...${NC}"
    echo "Configuration: ${threads} threads, ${ramp_up}s ramp-up, ${duration}s duration"

    local result_file="${RESULTS_DIR}/${test_name}_${thread_type}_$(date +%Y%m%d_%H%M%S).jtl"

    # Wait for service to be ready
    echo "Checking if order-service is ready..." + http://${ORDER_SERVICE_HOST}:${ORDER_SERVICE_PORT}/actuator/health
    until curl -f http://${ORDER_SERVICE_HOST}:${ORDER_SERVICE_PORT}/actuator/health >/dev/null 2>&1; do
        echo "Waiting for order-service to be ready..."
        sleep 5
    done
    echo -e "${GREEN}Order service is ready!${NC}"

    # Run JMeter test
    jmeter -n -t ${TEST_PLAN} \
        -JORDER_SERVICE_HOST=${ORDER_SERVICE_HOST} \
        -JORDER_SERVICE_PORT=${ORDER_SERVICE_PORT} \
        -JTHREADS=${threads} \
        -JRAMP_UP=${ramp_up} \
        -JDURATION=${duration} \
        -l ${result_file} \
        -e -o ${RESULTS_DIR}/${test_name}_${thread_type}_dashboard

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Test ${test_name} completed successfully!${NC}"
        echo "Results saved to: ${result_file}"
        echo "Dashboard created in: ${RESULTS_DIR}/${test_name}_${thread_type}_dashboard"

        # Generate summary
        generate_summary ${result_file} ${test_name} ${thread_type}
    else
        echo -e "${RED}Test ${test_name} failed!${NC}"
    fi
}

# Function to generate summary
generate_summary() {
    local result_file=$1
    local test_name=$2
    local thread_type=$3

    if [ -f ${result_file} ]; then
        echo -e "\n${GREEN}=== Test Summary for ${test_name} (${thread_type}) ===${NC}"

        # Extract key metrics using awk
        awk -F',' 'NR>1 {
            count++
            if ($8 == "true") success++
            total_time += $2
            if ($2 > max_time) max_time = $2
            if (min_time == 0 || $2 < min_time) min_time = $2
        } END {
            if (count > 0) {
                printf "Total Requests: %d\n", count
                printf "Successful Requests: %d (%.2f%%)\n", success, (success/count)*100
                printf "Failed Requests: %d (%.2f%%)\n", count-success, ((count-success)/count)*100
                printf "Average Response Time: %.2f ms\n", total_time/count
                printf "Min Response Time: %.2f ms\n", min_time
                printf "Max Response Time: %.2f ms\n", max_time
            }
        }' ${result_file}
    fi
}

# Main execution
echo "Available test scenarios:"
for scenario in "${!SCENARIOS[@]}"; do
    echo "  - ${scenario}: ${SCENARIOS[$scenario]}"
done

# Check if thread type is provided
THREAD_TYPE=${1:-"standard"}
if [ "$THREAD_TYPE" != "standard" ] && [ "$THREAD_TYPE" != "virtual" ]; then
    echo -e "${RED}Invalid thread type. Use 'standard' or 'virtual'${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Testing with ${THREAD_TYPE} threads${NC}"

# Run all scenarios
for scenario in "${!SCENARIOS[@]}"; do
    IFS=' ' read -ra CONFIG <<< "${SCENARIOS[$scenario]}"
    run_test $scenario ${CONFIG[0]} ${CONFIG[1]} ${CONFIG[2]} $THREAD_TYPE

    # Wait between tests
    echo "Waiting 30 seconds before next test..."
    sleep 30
done

echo -e "\n${GREEN}All tests completed!${NC}"
echo "Results are available in: ${RESULTS_DIR}"