package lol.cqllmetoxic.nullpointerentity.entity.client;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.entity.FakePlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

/**
 * renders the fake player entity using player model.
 * uses custom texture for the nullpointer entity appearance.
 * provides client-side rendering for the entity that appears behind players.
 */
public class FakePlayerEntityRenderer extends MobEntityRenderer<FakePlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {

    private static final Identifier TEXTURE = Identifier.of(NullPointerEntity.MOD_ID, "textures/entity/player/nullpointer_entity.png");

    /**
     * creates a new fake player entity renderer.
     *
     * @param context the entity renderer factory context
     */
    public FakePlayerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }
}
