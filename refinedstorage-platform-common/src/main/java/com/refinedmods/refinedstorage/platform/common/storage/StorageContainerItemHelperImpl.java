package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongFunction;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageContainerItemHelperImpl implements StorageContainerItemHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageContainerItemHelperImpl.class);

    private final Map<Item, ResourceLocation> diskModelsByItem = new HashMap<>();
    private final Set<ResourceLocation> diskModels = new HashSet<>();

    @Override
    public Optional<SerializableStorage> resolveStorage(final StorageRepository storageRepository,
                                                        final ItemStack stack) {
        return getId(stack).flatMap(storageRepository::get);
    }

    @Override
    public void setStorage(final StorageRepository storageRepository,
                           final ItemStack stack,
                           final SerializableStorage storage) {
        final UUID id = UUID.randomUUID();
        setId(stack, id);
        storageRepository.set(id, storage);
    }

    @Override
    public boolean hasStorage(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getStorageReference());
    }

    @Override
    public Optional<StorageInfo> getInfo(final StorageRepository storageRepository, final ItemStack stack) {
        return getId(stack).map(storageRepository::getInfo);
    }

    @Override
    public InteractionResultHolder<ItemStack> tryDisassembly(final Level level,
                                                             final Player player,
                                                             final ItemStack stack,
                                                             final ItemStack primaryByproduct,
                                                             @Nullable final ItemStack secondaryByproduct) {
        if (!(level instanceof ServerLevel) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.fail(stack);
        }

        final Optional<UUID> storageId = getId(stack);
        if (storageId.isEmpty()) {
            return returnByproducts(level, player, primaryByproduct, secondaryByproduct);
        }

        return storageId
            .flatMap(id -> PlatformApi.INSTANCE.getStorageRepository(level).removeIfEmpty(id))
            .map(disk -> returnByproducts(level, player, primaryByproduct, secondaryByproduct))
            .orElseGet(() -> InteractionResultHolder.fail(stack));
    }

    private InteractionResultHolder<ItemStack> returnByproducts(final Level level,
                                                                final Player player,
                                                                final ItemStack primaryByproduct,
                                                                @Nullable final ItemStack secondaryByproduct) {
        tryReturnByproductToInventory(level, player, secondaryByproduct);
        return InteractionResultHolder.success(primaryByproduct);
    }

    private static void tryReturnByproductToInventory(final Level level,
                                                      final Player player,
                                                      @Nullable final ItemStack byproduct) {
        if (byproduct != null && !player.getInventory().add(byproduct.copy())) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), byproduct));
        }
    }

    @Override
    public void appendToTooltip(final ItemStack stack,
                                final StorageRepository storageRepository,
                                final List<Component> tooltip,
                                final TooltipFlag context,
                                final LongFunction<String> amountFormatter,
                                final boolean hasCapacity) {
        getInfo(storageRepository, stack).ifPresent(info -> {
            if (hasCapacity) {
                StorageTooltipHelper.addAmountStoredWithCapacity(
                    tooltip,
                    info.stored(),
                    info.capacity(),
                    amountFormatter
                );
            } else {
                StorageTooltipHelper.addAmountStoredWithoutCapacity(
                    tooltip,
                    info.stored(),
                    amountFormatter
                );
            }
        });
        if (context.isAdvanced()) {
            getId(stack).ifPresent(id -> {
                final MutableComponent idComponent = Component.literal(id.toString()).withStyle(ChatFormatting.GRAY);
                tooltip.add(idComponent);
            });
        }
    }

    @Override
    public void transferToBlockEntity(final ItemStack stack, final StorageBlockEntity blockEntity) {
        getId(stack).ifPresent(id -> {
            blockEntity.setStorageId(id);
            LOGGER.debug("Transferred storage {} to block entity {}", id, blockEntity);
        });
    }

    @Override
    public void transferFromBlockEntity(final ItemStack stack, final StorageBlockEntity blockEntity) {
        final UUID storageId = blockEntity.getStorageId();
        if (storageId != null) {
            LOGGER.debug("Transferred storage {} from block entity {} to stack", storageId, blockEntity);
            setId(stack, storageId);
        } else {
            LOGGER.warn("Could not transfer storage {} to stack, there is no storage ID!", blockEntity);
        }
    }

    @Override
    public void registerDiskModel(final Item item, final ResourceLocation model) {
        diskModelsByItem.put(item, model);
        diskModels.add(model);
    }

    @Override
    public Set<ResourceLocation> getDiskModels() {
        return diskModels;
    }

    @Override
    public Map<Item, ResourceLocation> getDiskModelsByItem() {
        return Collections.unmodifiableMap(diskModelsByItem);
    }

    private Optional<UUID> getId(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getStorageReference()));
    }

    private void setId(final ItemStack stack, final UUID id) {
        stack.set(DataComponents.INSTANCE.getStorageReference(), id);
    }
}
