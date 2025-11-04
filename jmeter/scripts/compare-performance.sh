#!/bin/bash

# Script to compare performance between standard and virtual threads

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${GREEN}=== Performance Comparison Tool ===${NC}"

RESULTS_DIR="/results"

# Function to analyze JTL file
analyze_results() {
    local file=$1
    local label=$2

    if [ ! -f "$file" ]; then
        echo -e "${RED}File not found: $file${NC}"
        return 1
    fi

    echo -e "\n${BLUE}=== $label ===${NC}"

    awk -F',' 'NR>1 {
        count++
        if ($8 == "true") success++
        total_time += $2
        if ($2 > max_time) max_time = $2
        if (min_time == 0 || $2 < min_time) min_time = $2

        # Calculate percentiles (simplified)
        times[count] = $2
    } END {
        if (count > 0) {
            # Sort times for percentile calculation
            n = asort(times)
            p90_idx = int(n * 0.9)
            p95_idx = int(n * 0.95)
            p99_idx = int(n * 0.99)

            printf "Total Requests: %d\n", count
            printf "Success Rate: %.2f%%\n", (success/count)*100
            printf "Throughput: %.2f req/sec\n", count/300  # Assuming 5 min test
            printf "Avg Response Time: %.2f ms\n", total_time/count
            printf "Min Response Time: %.2f ms\n", min_time
            printf "Max Response Time: %.2f ms\n", max_time
            printf "90th Percentile: %.2f ms\n", times[p90_idx]
            printf "95th Percentile: %.2f ms\n", times[p95_idx]
            printf "99th Percentile: %.2f ms\n", times[p99_idx]
        }
    }' "$file"
}

# Compare specific test scenario
compare_scenario() {
    local scenario=$1
    echo -e "\n${YELLOW}=== Comparing $scenario ===${NC}"

    # Find latest results for each thread type
    standard_file=$(ls -t ${RESULTS_DIR}/${scenario}_standard_*.jtl 2>/dev/null | head -1)
    virtual_file=$(ls -t ${RESULTS_DIR}/${scenario}_virtual_*.jtl 2>/dev/null | head -1)

    if [ -z "$standard_file" ]; then
        echo -e "${RED}No standard thread results found for $scenario${NC}"
        return 1
    fi

    if [ -z "$virtual_file" ]; then
        echo -e "${RED}No virtual thread results found for $scenario${NC}"
        return 1
    fi

    analyze_results "$standard_file" "Standard Threads"
    analyze_results "$virtual_file" "Virtual Threads"
}

# Main comparison
scenarios=("light_load" "medium_load" "heavy_load" "stress_test")

for scenario in "${scenarios[@]}"; do
    compare_scenario $scenario
done

echo -e "\n${GREEN}Performance comparison completed!${NC}"