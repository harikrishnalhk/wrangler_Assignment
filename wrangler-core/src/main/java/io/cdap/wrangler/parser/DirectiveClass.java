/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenGroup;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.parser.DirectivesParser.BoolContext;
import io.cdap.wrangler.parser.DirectivesParser.ByteSizeContext;
import io.cdap.wrangler.parser.DirectivesParser.ColumnContext;
import io.cdap.wrangler.parser.DirectivesParser.NumberContext;
import io.cdap.wrangler.parser.DirectivesParser.TextContext;
import io.cdap.wrangler.parser.DirectivesParser.TimeDurationContext;
import io.cdap.wrangler.parser.DirectivesParser.ValueContext;

/**
 * Extended visitor implementation that converts ANTLR parse tree nodes into Wrangler's Token objects.
 */
public class DirectivesVisitor extends DirectivesBaseVisitor<TokenGroup> {

    @Override
    public TokenGroup visitValue(ValueContext ctx) {
        if (ctx.String() != null) {
            return new TokenGroup(new Text(removeQuotes(ctx.String().getText())));
        } else if (ctx.Number() != null) {
            return visitNumber(ctx.Number());
        } else if (ctx.Column() != null) {
            return visitColumn(ctx.Column());
        } else if (ctx.Bool() != null) {
            return visitBool(ctx.Bool());
        } else if (ctx.byteSize() != null) {
            return visitByteSize(ctx.byteSize());
        } else if (ctx.timeDuration() != null) {
            return visitTimeDuration(ctx.timeDuration());
        }
        throw new IllegalStateException("Unknown value type: " + ctx.getText());
    }

    @Override
    public TokenGroup visitByteSize(ByteSizeContext ctx) {
        String text = ctx.BYTE_SIZE().getText();
        try {
            return new TokenGroup(new ByteSize(text), TokenType.BYTE_SIZE);
        } catch (IllegalArgumentException e) {
            throw new DirectiveParseException(
                String.format("Invalid byte size format '%s'. Expected format like '10MB' or '1.5GiB'", text), e);
        }
    }

    @Override
    public TokenGroup visitTimeDuration(TimeDurationContext ctx) {
        String text = ctx.TIME_DURATION().getText();
        try {
            return new TokenGroup(new TimeDuration(text), TokenType.TIME_DURATION);
        } catch (IllegalArgumentException e) {
            throw new DirectiveParseException(
                String.format("Invalid time duration format '%s'. Expected format like '500ms' or '2s'", text), e);
        }
    }

    @Override
    public TokenGroup visitColumn(ColumnContext ctx) {
        return new TokenGroup(new ColumnName(ctx.Column().getText()), TokenType.COLUMN_NAME);
    }

    @Override
    public TokenGroup visitNumber(NumberContext ctx) {
        String text = ctx.Number().getText();
        try {
            if (text.contains(".")) {
                return new TokenGroup(Double.parseDouble(text), TokenType.NUMERIC);
            }
            return new TokenGroup(Long.parseLong(text), TokenType.NUMERIC);
        } catch (NumberFormatException e) {
            throw new DirectiveParseException("Invalid number format: " + text, e);
        }
    }

    @Override
    public TokenGroup visitText(TextContext ctx) {
        return new TokenGroup(new Text(removeQuotes(ctx.String().getText())), TokenType.TEXT);
    }

    @Override
    public TokenGroup visitBool(BoolContext ctx) {
        return new TokenGroup(Boolean.parseBoolean(ctx.Bool().getText()), TokenType.BOOLEAN);
    }

    private String removeQuotes(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }
}
