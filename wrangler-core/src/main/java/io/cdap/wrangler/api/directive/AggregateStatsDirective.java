package io.cdap.wrangler.directive;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.executor.RecipePipeline;
import io.cdap.wrangler.executor.RecipePipelineExecutor;
import io.cdap.wrangler.parser.GrammarBasedParser;
import io.cdap.wrangler.parser.RecipeParser;
import io.cdap.wrangler.parser.TextDirectives;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AggregateStatsDirectiveTest {
    
    @Test
    public void testTotalAggregation() throws Exception {
        // Test data from PDF specification
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "1MB").add("response_time", "500ms"),
            new Row("data_transfer_size", "2MB").add("response_time", "300ms"),
            new Row("data_transfer_size", "500KB").add("response_time", "200ms")
        );

        // Recipe from PDF specification
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        RecipeParser parser = new GrammarBasedParser(new TextDirectives(recipe));
        RecipePipeline pipeline = new RecipePipelineExecutor();
        List<Row> results = pipeline.execute(parser, rows);

        // Assertions from PDF specification
        assertEquals(1, results.size());
        assertEquals(3.5, results.get(0).getValue("total_size_mb"), 0.001);
        assertEquals(1.0, results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testAverageAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("size", "1MB").add("time", "1s"),
            new Row("size", "2MB").add("time", "2s")
        );

        String[] recipe = new String[] {
            "aggregate-stats :size :time avg_size_mb avg_time_sec MB S average"
        };

        RecipeParser parser = new GrammarBasedParser(new TextDirectives(recipe));
        RecipePipeline pipeline = new RecipePipelineExecutor();
        List<Row> results = pipeline.execute(parser, rows);

        assertEquals(1, results.size());
        assertEquals(1.5, results.get(0).getValue("avg_size_mb"), 0.001);
        assertEquals(1.5, results.get(0).getValue("avg_time_sec"), 0.001);
    }

    @Test(expected = DirectiveExecutionException.class)
    public void testInvalidData() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("size", "invalid").add("time", "1s")
        );

        String[] recipe = new String[] {
            "aggregate-stats :size :time out_size out_time"
        };

        RecipePipelineExecutor.execute(recipe, rows);
    }
}
