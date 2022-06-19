package com.refinedmods.refinedstorage2.platform.apiimpl.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.test.SetupMinecraft;
import com.refinedmods.refinedstorage2.test.Rs2Test;
import com.refinedmods.refinedstorage2.test.SimpleListener;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.platform.test.TagHelper.createDummyTag;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
@SetupMinecraft
class FluidStorageTypeTest {
    FluidStorageType sut = FluidStorageType.INSTANCE;
    SimpleListener listener;

    @BeforeEach
    void setUp() {
        listener = new SimpleListener();
    }

    @Test
    void Test_serialization_of_regular_storage() {
        // Arrange
        InMemoryTrackedStorageRepository<FluidResource> tracker = new InMemoryTrackedStorageRepository<>();
        Storage<FluidResource> storage = new PlatformStorage<>(
                new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), tracker, () -> 123L),
                FluidStorageType.INSTANCE,
                tracker,
                () -> {
                }
        );

        storage.insert(new FluidResource(Fluids.WATER, createDummyTag()), 10, Action.EXECUTE, new PlayerSource("A"));
        storage.insert(new FluidResource(Fluids.LAVA, null), 15, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        CompoundTag serialized = sut.toTag(storage);
        Storage<FluidResource> deserialized = sut.fromTag(serialized, listener);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized).isInstanceOf(PlatformStorage.class);
        assertThat(deserialized.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>(new FluidResource(Fluids.WATER, createDummyTag()), 10),
                new ResourceAmount<>(new FluidResource(Fluids.LAVA, null), 15)
        );
        assertThat(((TrackedStorage<FluidResource>) deserialized).findTrackedResourceBySourceType(new FluidResource(Fluids.WATER, createDummyTag()), PlayerSource.class)).get().usingRecursiveComparison().isEqualTo(new TrackedResource("A", 123));
        assertThat(((TrackedStorage<FluidResource>) deserialized).findTrackedResourceBySourceType(new FluidResource(Fluids.LAVA, null), PlayerSource.class)).isEmpty();
    }

    @Test
    void Test_passing_listener_to_deserialized_storage() {
        // Arrange
        InMemoryTrackedStorageRepository<FluidResource> tracker = new InMemoryTrackedStorageRepository<>();
        Storage<FluidResource> storage = new PlatformStorage<>(
                new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), tracker, () -> 123L),
                FluidStorageType.INSTANCE,
                tracker,
                () -> {
                }
        );
        storage.insert(new FluidResource(Fluids.WATER, null), 15, Action.EXECUTE, EmptySource.INSTANCE);

        CompoundTag serialized = sut.toTag(storage);
        Storage<FluidResource> deserialized = sut.fromTag(serialized, listener);

        // Act
        boolean preInsert = listener.isChanged();
        deserialized.insert(new FluidResource(Fluids.WATER, null), 15, Action.EXECUTE, EmptySource.INSTANCE);
        boolean postInsert = listener.isChanged();

        // Assert
        assertThat(preInsert).isFalse();
        assertThat(postInsert).isTrue();
    }

    @Test
    void Test_serialization_of_limited_storage() {
        // Arrange
        InMemoryTrackedStorageRepository<FluidResource> tracker = new InMemoryTrackedStorageRepository<>();
        Storage<FluidResource> storage = new LimitedPlatformStorage<>(
                new LimitedStorageImpl<>(new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), tracker, () -> 123L), 100),
                FluidStorageType.INSTANCE,
                tracker,
                () -> {
                }
        );

        storage.insert(new FluidResource(Fluids.WATER, createDummyTag()), 10, Action.EXECUTE, new PlayerSource("A"));
        storage.insert(new FluidResource(Fluids.LAVA, null), 15, Action.EXECUTE, EmptySource.INSTANCE);

        // Act
        CompoundTag serialized = sut.toTag(storage);
        Storage<FluidResource> deserialized = sut.fromTag(serialized, listener);

        // Assert
        assertThat(listener.isChanged()).isFalse();
        assertThat(deserialized).isInstanceOf(LimitedStorage.class);
        assertThat(((LimitedStorage<FluidResource>) deserialized).getCapacity()).isEqualTo(100);
        assertThat(deserialized.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount<>(new FluidResource(Fluids.WATER, createDummyTag()), 10),
                new ResourceAmount<>(new FluidResource(Fluids.LAVA, null), 15)
        );
        assertThat(((TrackedStorage<FluidResource>) deserialized).findTrackedResourceBySourceType(new FluidResource(Fluids.WATER, createDummyTag()), PlayerSource.class)).get().usingRecursiveComparison().isEqualTo(new TrackedResource("A", 123));
        assertThat(((TrackedStorage<FluidResource>) deserialized).findTrackedResourceBySourceType(new FluidResource(Fluids.LAVA, null), PlayerSource.class)).isEmpty();
    }
}