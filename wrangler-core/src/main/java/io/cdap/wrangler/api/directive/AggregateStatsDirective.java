package io.cdap.wrangler.api.directive;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Directive for aggregating byte size and time duration statistics.
 * Example usage:
 * aggregate-stats :data_size :response_time total_size_mb total_time_sec MB S
 */
public class AggregateStatsDirective implements Directive {
    public static final String DIRECTIVE_NAME = "aggregate-stats";
    
    private String sizeColumn;
    private String timeColumn;
    private String sizeOutputColumn;
    private String timeOutputColumn;
    private String sizeOutputUnit = "MB";
    private String timeOutputUnit = "S";
    private String aggregationType = "total";
    
    // Aggregation stores
    private long totalBytes = 0;
    private long totalNanos = 0;
    private int rowCount = 0;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder(DIRECTIVE_NAME)
            .define("size-column", TokenType.COLUMN_NAME)
            .define("time-column", TokenType.COLUMN_NAME)
            .define("size-output", TokenType.COLUMN_NAME)
            .define("time-output", TokenType.COLUMN_NAME)
            .define("size-unit", TokenType.TEXT, Optional.TRUE)
            .define("time-unit", TokenType.TEXT, Optional.TRUE)
            .define("aggregation", TokenType.TEXT, Optional.TRUE)
            .build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((ColumnName) args.value("size-column")).value();
        this.timeColumn = ((ColumnName) args.value("time-column")).value();
        this.sizeOutputColumn = ((ColumnName) args.value("size-output")).value();
        this.timeOutputColumn = ((ColumnName) args.value("time-output")).value();
        
        if (args.contains("size-unit")) {
            this.sizeOutputUnit = ((Text) args.value("size-unit")).value().toUpperCase();
        }
        if (args.contains("time-unit")) {
            this.timeOutputUnit = ((Text) args.value("time-unit")).value().toUpperCase();
        }
        if (args.contains("aggregation")) {
            this.aggregationType = ((Text) args.value("aggregation")).value().toLowerCase();
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        for (Row row : rows) {
            try {
                Object sizeValue = row.getValue(sizeColumn);
                if (sizeValue != null) {
                    ByteSize size = new ByteSize(sizeValue.toString());
                    totalBytes += size.getBytes();
                }
                
                Object timeValue = row.getValue(timeColumn);
                if (timeValue != null) {
                    TimeDuration time = new TimeDuration(timeValue.toString());
                    totalNanos += time.getNanoseconds();
                }
                
                rowCount++;
            } catch (Exception e) {
                throw new DirectiveExecutionException(
                    String.format("Error processing row %d: %s", rowCount + 1, e.getMessage()), e);
            }
        }
        return rows;
    }

    @Override
    public List<Row> finalize(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        Row result = new Row();
        
        // Calculate size aggregate
        double sizeResult = convertBytes(totalBytes, sizeOutputUnit);
        if ("average".equals(aggregationType) && rowCount > 0) {
            sizeResult = sizeResult / rowCount;
        }
        result.add(sizeOutputColumn, sizeResult);
        
        // Calculate time aggregate
        double timeResult = convertNanos(totalNanos, timeOutputUnit);
        if ("average".equals(aggregationType) && rowCount > 0) {
            timeResult = timeResult / rowCount;
        }
        result.add(timeOutputColumn, timeResult);
        
        return Collections.singletonList(result);
    }

    private double convertBytes(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "KB": return bytes / 1000.0;
            case "MB": return bytes / (1000.0 * 1000);
            case "GB": return bytes / (1000.0 * 1000 * 1000);
            case "TB": return bytes / (1000.0 * 1000 * 1000 * 1000);
            case "PB": return bytes / (1000.0 * 1000 * 1000 * 1000 * 1000);
            case "KIB": return bytes / 1024.0;
            case "MIB": return bytes / (1024.0 * 1024);
            case "GIB": return bytes / (1024.0 * 1024 * 1024);
            case "TIB": return bytes / (1024.0 * 1024 * 1024 * 1024);
            case "PIB": return bytes / (1024.0 * 1024 * 1024 * 1024 * 1024);
            case "B":
            default: return bytes;
        }
    }

    private double convertNanos(long nanos, String unit) {
        switch (unit.toUpperCase()) {
            case "NS": return nanos;
            case "US": return nanos / 1000.0;
            case "MS": return nanos / (1000.0 * 1000);
            case "S": return nanos / (1000.0 * 1000 * 1000);
            case "M": return nanos / (1000.0 * 1000 * 1000 * 60);
            case "H": return nanos / (1000.0 * 1000 * 1000 * 60 * 60);
            case "D": return nanos / (1000.0 * 1000 * 1000 * 60 * 60 * 24);
            default: return nanos / (1000.0 * 1000 * 1000); // default to seconds
        }
    }

    @Override
    public void destroy() {
        // Reset aggregation state
        totalBytes = 0;
        totalNanos = 0;
        rowCount = 0;
    }
}
