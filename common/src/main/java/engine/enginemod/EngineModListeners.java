package engine.enginemod;

import engine.Platform;
import engine.block.AirBlock;
import engine.block.Block;
import engine.entity.CameraEntity;
import engine.entity.EntityProvider;
import engine.entity.item.ItemEntity;
import engine.event.Listener;
import engine.event.mod.ModLifecycleEvent;
import engine.event.mod.ModRegistrationEvent;
import engine.item.Item;
import engine.registry.game.*;
import engine.registry.impl.IdAutoIncreaseRegistry;
import engine.server.event.NetworkingStartEvent;
import engine.server.network.packet.*;
import engine.server.network.packet.c2s.PacketLoginProfile;
import engine.server.network.packet.c2s.PacketPlayerAction;
import engine.server.network.packet.c2s.PacketPlayerMove;
import engine.server.network.packet.c2s.PacketTwoHandComponentChange;
import engine.server.network.packet.s2c.*;
import engine.world.WorldProvider;
import engine.world.provider.FlatWorldProvider;

public final class EngineModListeners {

    @Listener
    public static void onPreInit(ModLifecycleEvent.PreInitialization event) {
        Platform.getEngine().getEventBus().register(EngineModListeners.class);
    }

    @Listener
    public static void onNetworkingStart(NetworkingStartEvent event) {
        event.getNetworkingEventBus().register(ServerHandlingListeners.class);
    }

    @Listener
    public static void constructRegistry(ModRegistrationEvent.Construction e) {
        // TODO: move to common.
        e.addRegistry(WorldProvider.class, () -> new IdAutoIncreaseRegistry<>(WorldProvider.class));
        e.addRegistry(Block.class, BlockRegistryImpl::new);
        e.addRegistry(Item.class, ItemRegistryImpl::new);
        e.addRegistry(EntityProvider.class, EntityRegistryImpl::new);
        e.addRegistry(PacketProvider.class, PacketRegistry::new);
    }

    @Listener
    public static void registerWorldProvider(ModRegistrationEvent.Register<WorldProvider> event) {
        event.register(new FlatWorldProvider().name("flat"));
    }

    @Listener
    public static void registerBlocks(ModRegistrationEvent.Register<Block> event) {
        event.register(AirBlock.AIR);
        ((BlockRegistry) event.getRegistry()).setAirBlock(AirBlock.AIR);
    }

    @Listener
    public static void registerEntities(ModRegistrationEvent.Register<EntityProvider> event) {
        event.register(new EntityProvider(CameraEntity.class, CameraEntity::new, null).name("camera"));
        event.register(new EntityProvider(ItemEntity.class, ItemEntity::new, null).name("item"));
    }

    @Listener
    public static void registerPacket(ModRegistrationEvent.Register<PacketProvider> event){
        event.register(new PacketProvider.Builder().type(PacketRaw.class).name("raw").build());
        event.register(new PacketProvider.Builder().type(PacketHandshake.class).name("handshake").build());
        event.register(new PacketProvider.Builder().type(PacketDisconnect.class).name("disconnect").build());
        event.register(new PacketProvider.Builder().type(PacketAlive.class).name("alive").build());
        event.register(new PacketProvider.Builder().type(PacketLoginRequest.class).name("login-request").build());
        event.register(new PacketProvider.Builder().type(PacketLoginProfile.class).name("login-profile").build());
        event.register(new PacketProvider.Builder().type(PacketLoginSuccess.class).name("login-success").build());
        event.register(new PacketProvider.Builder().type(PacketSyncRegistry.class).name("registry-sync").build());
        event.register(new PacketProvider.Builder().type(PacketGameData.class).name("game-data").build());
        event.register(new PacketProvider.Builder().type(PacketChunkData.class).name("chunk-data").build());
        event.register(new PacketProvider.Builder().type(PacketUnloadChunk.class).name("chunk-unload").build());
        event.register(new PacketProvider.Builder().type(PacketBlockUpdate.class).name("block-update").build());
        event.register(new PacketProvider.Builder().type(PacketPlayerMove.class).name("player-move").build());
        event.register(new PacketProvider.Builder().type(PacketPlayerPosView.class).name("player-posview").build());
        event.register(new PacketProvider.Builder().type(PacketPlayerPosView.Confirmed.class).name("player-posview-confirm").build());
        event.register(new PacketProvider.Builder().type(PacketPlayerAction.class).name("player-action").build());

        //TODO: not a good place to do so?
        event.register(new PacketProvider.Builder().type(PacketTwoHandComponentChange.class).name("two-hand-change").build());
    }

}
