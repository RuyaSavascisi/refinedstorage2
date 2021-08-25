package com.refinedmods.refinedstorage2.api.grid.query;

import com.refinedmods.refinedstorage2.api.grid.GridStack;

import java.util.function.Predicate;

public interface GridQueryParser {
    Predicate<GridStack<?>> parse(String query) throws GridQueryParserException;
}