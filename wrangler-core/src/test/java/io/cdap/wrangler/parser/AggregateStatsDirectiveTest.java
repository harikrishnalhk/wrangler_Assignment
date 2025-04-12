package io.cdap.wrangler;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
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
    public void testAggregateStats() throws Exception {
        List<Row> rows = Arrays.asList(
            new Row("data_transfer_size", "1MB").add("response_time", "500ms"),
            new Row("data_transfer_size", "2MB").add("response_time", "300ms"),
            new Row("data_transfer_size", "500KB").add("response_time", "200ms")
        );

        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        RecipeParser parser = new GrammarBasedParser(new TextDirectives(recipe));
        RecipePipeline pipeline = new RecipePipelineExecutor();
        List<Row> results = pipeline.execute(parser, rows);

        assertEquals(1, results.size());
        assertEquals(3.5, results.get(0).getValue("total_size_mb"));
        assertEquals(1.0, results.get(0).getValue("total_time_sec"));
    }
}
