package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Set;

public class FakeGridResource extends GridResource<String> {
    public FakeGridResource(String name, long amount) {
        this(new ResourceAmount<>(name, amount));
    }

    public FakeGridResource(String name) {
        this(new ResourceAmount<>(name, 1));
    }

    public FakeGridResource(ResourceAmount<String> resourceAmount) {
        super(
                resourceAmount,
                resourceAmount.getResource(),
                resourceAmount.getResource(),
                resourceAmount.getResource(),
                Set.of()
        );
    }

    public FakeGridResource(String name, long amount, String modId, String modName, Set<String> tags) {
        super(new ResourceAmount<>(name, amount), name, modId, modName, tags);
    }

    public FakeGridResource zeroed() {
        setZeroed(true);
        return this;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String toString() {
        return super.getResourceAmount().toString();
    }
}