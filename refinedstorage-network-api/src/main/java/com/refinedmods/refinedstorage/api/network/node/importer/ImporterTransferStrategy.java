package com.refinedmods.refinedstorage.api.network.node.importer;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;

import org.apiguardian.api.API;

/**
 * An importer transfer strategy transfers resources from a source to the network.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface ImporterTransferStrategy {
    boolean transfer(Filter filter, Actor actor, Network network);
}
