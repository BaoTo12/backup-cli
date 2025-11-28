package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.adapter.in.cli.service.ConsoleService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.concurrent.TimeUnit;

@ShellComponent
@RequiredArgsConstructor
public class MetricsCliAdapter {

    private final MeterRegistry meterRegistry;
    private final ConsoleService consoleService;

    @ShellMethod(value = "Show backup metrics", key = "metrics")
    public void showMetrics() {
        consoleService.printInfo("Gathering metrics...");

        System.out.println();
        System.out.println("📊 " + consoleService.formatKey("BACKUP STATISTICS"));
        System.out.println("------------------------------------------------");

        displayCounter("dbbackup.backup.total", "Total Backups", "status", "success");
        displayCounter("dbbackup.backup.total", "Failed Backups", "status", "failure");

        System.out.println();
        displayTimer("dbbackup.backup.duration", "Avg Backup Duration (Success)", "result", "success");
        displayTimer("dbbackup.backup.duration", "Avg Backup Duration (Failure)", "result", "failure");

        System.out.println();
        displayCounter("dbbackup.retry.total", "Total Retries", null, null);

        System.out.println("------------------------------------------------");
    }

    private void displayCounter(String metricName, String label, String tagKey, String tagValue) {
        Search search = meterRegistry.find(metricName);
        if (tagKey != null) {
            search.tag(tagKey, tagValue);
        }
        Counter counter = search.counter();

        double count = (counter != null) ? counter.count() : 0;
        System.out.printf("%-30s: %s%n", label, consoleService.formatValue(String.format("%.0f", count)));
    }

    private void displayTimer(String metricName, String label, String tagKey, String tagValue) {
        Search search = meterRegistry.find(metricName);
        if (tagKey != null) {
            search.tag(tagKey, tagValue);
        }
        Timer timer = search.timer();

        double mean = (timer != null) ? timer.mean(TimeUnit.MILLISECONDS) : 0;
        double max = (timer != null) ? timer.max(TimeUnit.MILLISECONDS) : 0;

        System.out.printf("%-30s: %s ms (Max: %s ms)%n", label,
                consoleService.formatValue(String.format("%.2f", mean)),
                consoleService.formatValue(String.format("%.2f", max)));
    }
}
