package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractionsProxy;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacadeProxy;
import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.common.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.Rs2PlatformApiFacadeImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.item.CoreItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorBindingItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.item.QuartzEnrichedIronItem;
import com.refinedmods.refinedstorage2.platform.common.item.SiliconItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.NameableBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.forge.internal.PlatformAbstractionsImpl;

import net.minecraft.core.Direction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer {
    private static final String BLOCK_TRANSLATION_CATEGORY = "block";
    private static final CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(IdentifierUtil.MOD_ID + ".general") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.INSTANCE.getController().getNormal());
        }
    };

    public ModInitializer() {
        initializePlatformAbstractions();
        initializePlatformApiFacade();
        registerStorageChannelTypes();
        registerNetworkComponents();
        registerResourceTypes();
        registerTickHandler();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onClientSetup);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, this::registerBlockEntityTypes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerMenus);

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerCapabilities);
    }

    private void initializePlatformAbstractions() {
        ((PlatformAbstractionsProxy) PlatformAbstractions.INSTANCE).setAbstractions(new PlatformAbstractionsImpl());
    }

    private void initializePlatformApiFacade() {
        ((Rs2PlatformApiFacadeProxy) Rs2PlatformApiFacade.INSTANCE).setFacade(new Rs2PlatformApiFacadeImpl());
    }

    private void registerStorageChannelTypes() {
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.ITEM);
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.FLUID);
    }

    private void registerNetworkComponents() {
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentRegistry().addComponent(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentRegistry().addComponent(GraphNetworkComponent.class, GraphNetworkComponent::new);
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentRegistry().addComponent(StorageNetworkComponent.class, network -> new StorageNetworkComponent(StorageChannelTypeRegistry.INSTANCE));
    }

    private void registerResourceTypes() {
        Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().register(FluidResourceType.INSTANCE);
    }

    private void registerTickHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> e) {
        CableBlock cableBlock = new CableBlock();
        cableBlock.setRegistryName(createIdentifier("cable"));
        Blocks.INSTANCE.setCable(cableBlock);
        e.getRegistry().register(cableBlock);

        QuartzEnrichedIronBlock quartzEnrichedIronBlock = new QuartzEnrichedIronBlock();
        quartzEnrichedIronBlock.setRegistryName(createIdentifier("quartz_enriched_iron_block"));
        Blocks.INSTANCE.setQuartzEnrichedIron(quartzEnrichedIronBlock);
        e.getRegistry().register(quartzEnrichedIronBlock);

        DiskDriveBlock diskDriveBlock = new DiskDriveBlock();
        diskDriveBlock.setRegistryName(createIdentifier("disk_drive"));
        Blocks.INSTANCE.setDiskDrive(diskDriveBlock);
        e.getRegistry().register(diskDriveBlock);

        MachineCasingBlock machineCasingBlock = new MachineCasingBlock();
        machineCasingBlock.setRegistryName(createIdentifier("machine_casing"));
        Blocks.INSTANCE.setMachineCasing(machineCasingBlock);
        e.getRegistry().register(machineCasingBlock);

        Blocks.INSTANCE.getGrid().putAll(color -> {
            ItemGridBlock block = new ItemGridBlock(Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")));
            block.setRegistryName(Blocks.INSTANCE.getGrid().getId(color, "grid"));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getFluidGrid().putAll(color -> {
            FluidGridBlock block = new FluidGridBlock(Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")));
            block.setRegistryName(Blocks.INSTANCE.getFluidGrid().getId(color, "fluid_grid"));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getController().putAll(color -> {
            ControllerBlock block = new ControllerBlock(ControllerType.NORMAL, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")));
            block.setRegistryName(Blocks.INSTANCE.getController().getId(color, "controller"));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getCreativeController().putAll(color -> {
            ControllerBlock block = new ControllerBlock(ControllerType.CREATIVE, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")));
            block.setRegistryName(Blocks.INSTANCE.getCreativeController().getId(color, "creative_controller"));
            e.getRegistry().register(block);
            return block;
        });
    }

    @SubscribeEvent
    public void registerBlockEntityTypes(RegistryEvent.Register<BlockEntityType<?>> e) {
        BlockEntityType<CableBlockEntity> cableBlockEntityType = BlockEntityType.Builder.of(CableBlockEntity::new, Blocks.INSTANCE.getCable()).build(null);
        cableBlockEntityType.setRegistryName(createIdentifier("cable"));
        e.getRegistry().register(cableBlockEntityType);
        BlockEntities.INSTANCE.setCable(cableBlockEntityType);

        BlockEntityType<ControllerBlockEntity> controllerBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state), Blocks.INSTANCE.getController().toArray()).build(null);
        controllerBlockEntityType.setRegistryName(createIdentifier("controller"));
        e.getRegistry().register(controllerBlockEntityType);
        BlockEntities.INSTANCE.setController(controllerBlockEntityType);

        BlockEntityType<ControllerBlockEntity> creativeControllerBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state), Blocks.INSTANCE.getCreativeController().toArray()).build(null);
        creativeControllerBlockEntityType.setRegistryName(createIdentifier("creative_controller"));
        e.getRegistry().register(creativeControllerBlockEntityType);
        BlockEntities.INSTANCE.setCreativeController(creativeControllerBlockEntityType);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getCable(), createProperties()).setRegistryName(createIdentifier("cable")));
        e.getRegistry().register(new QuartzEnrichedIronItem(createProperties()).setRegistryName(createIdentifier("quartz_enriched_iron")));
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getQuartzEnrichedIron(), createProperties()).setRegistryName(createIdentifier("quartz_enriched_iron_block")));
        e.getRegistry().register(new SiliconItem(createProperties()).setRegistryName(createIdentifier("silicon")));
        e.getRegistry().register(new ProcessorBindingItem(createProperties()).setRegistryName(createIdentifier("processor_binding")));
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getDiskDrive(), createProperties()).setRegistryName(createIdentifier("disk_drive")));
        e.getRegistry().register(new WrenchItem(createProperties().stacksTo(1)).setRegistryName(createIdentifier("wrench")));

        StorageHousingItem storageHousingItem = new StorageHousingItem(createProperties());
        storageHousingItem.setRegistryName(createIdentifier("storage_housing"));
        e.getRegistry().register(storageHousingItem);
        Items.INSTANCE.setStorageHousing(storageHousingItem);

        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getMachineCasing(), createProperties()).setRegistryName(createIdentifier("machine_casing")));

        Blocks.INSTANCE.getGrid().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid"))).setRegistryName(Blocks.INSTANCE.getGrid().getId(color, "grid"))));
        Blocks.INSTANCE.getFluidGrid().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid"))).setRegistryName(Blocks.INSTANCE.getFluidGrid().getId(color, "fluid_grid"))));
        Blocks.INSTANCE.getController().forEach((color, block) -> {
            ControllerBlockItem controllerBlockItem = new ControllerBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")));
            controllerBlockItem.setRegistryName(Blocks.INSTANCE.getController().getId(color, "controller"));
            Items.INSTANCE.getControllers().add(controllerBlockItem);
            e.getRegistry().register(controllerBlockItem);
        });
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller"))).setRegistryName(Blocks.INSTANCE.getCreativeController().getId(color, "creative_controller"))));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            e.getRegistry().register(new ProcessorItem(createProperties()).setRegistryName(createIdentifier(type.getName() + "_processor")));
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            if (type != ItemStorageDiskItem.ItemStorageType.CREATIVE) {
                StoragePartItem storagePartItem = new StoragePartItem(createProperties());
                storagePartItem.setRegistryName(createIdentifier(type.getName() + "_storage_part"));
                e.getRegistry().register(storagePartItem);
                Items.INSTANCE.getStorageParts().put(type, storagePartItem);
            }
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            if (type != FluidStorageDiskItem.FluidStorageType.CREATIVE) {
                FluidStoragePartItem fluidStoragePartItem = new FluidStoragePartItem(createProperties());
                fluidStoragePartItem.setRegistryName(createIdentifier(type.getName() + "_fluid_storage_part"));
                e.getRegistry().register(fluidStoragePartItem);
                Items.INSTANCE.getFluidStorageParts().put(type, fluidStoragePartItem);
            }
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            e.getRegistry().register(new ItemStorageDiskItem(createProperties().stacksTo(1).fireResistant(), type).setRegistryName(createIdentifier(type.getName() + "_storage_disk")));
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            e.getRegistry().register(new FluidStorageDiskItem(createProperties().stacksTo(1).fireResistant(), type).setRegistryName(createIdentifier(type.getName() + "_fluid_storage_disk")));
        }

        e.getRegistry().register(new CoreItem(createProperties()).setRegistryName(createIdentifier("construction_core")));
        e.getRegistry().register(new CoreItem(createProperties()).setRegistryName(createIdentifier("destruction_core")));
    }

    @SubscribeEvent
    public void registerCapabilities(AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof ControllerBlockEntity controllerBlockEntity) {
            registerControllerEnergy(e, controllerBlockEntity);
        }
    }

    private void registerControllerEnergy(AttachCapabilitiesEvent<BlockEntity> e, ControllerBlockEntity controllerBlockEntity) {
        LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> (IEnergyStorage) controllerBlockEntity.getEnergyStorage());
        e.addCapability(createIdentifier("energy"), new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == CapabilityEnergy.ENERGY && controllerBlockEntity.getEnergyStorage() instanceof IEnergyStorage) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    private Item.Properties createProperties() {
        return new Item.Properties().tab(CREATIVE_MODE_TAB);
    }

    @SubscribeEvent
    public void registerMenus(RegistryEvent.Register<MenuType<?>> e) {
        MenuType<ControllerContainerMenu> controllerMenuType = IForgeMenuType.create(ControllerContainerMenu::new);
        controllerMenuType.setRegistryName(createIdentifier("controller"));
        e.getRegistry().register(controllerMenuType);
        Menus.INSTANCE.setController(controllerMenuType);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            TickHandler.runQueuedActions();
        }
    }
}