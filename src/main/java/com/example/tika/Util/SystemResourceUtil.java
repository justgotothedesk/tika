package com.example.tika.Util;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SystemResourceUtil {
    /**
     * 리소스 사용량 측정을 위한 메서드
     */
    public static List<Double> getSystemResources() {
        List<Double> resourceList = new ArrayList<>();

        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            double nowMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            double nowCPU = osBean.getProcessCpuLoad() * 100;

            resourceList.add(nowMemory);
            resourceList.add(nowCPU);
        } catch (Exception e) {
            log.error("Failed to cast OperatingSystemMXBean. System resource metrics may be unavailable.", e);
            resourceList.add(-1.0);
            resourceList.add(-1.0);
        }

        return resourceList;
    }
}
