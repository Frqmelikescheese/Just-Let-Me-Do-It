package com.frqme.just_let_me_do_it.client;
import com.frqme.just_let_me_do_it.Just_let_me_do_it;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EmissiveFeatureRenderer;
import net.minecraft.client.render.entity.model.CopperGolemEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.CopperGolemEntityRenderState;
import net.minecraft.util.Identifier;
public class CopperGolemEntityRenderer
        extends net.minecraft.client.render.entity.CopperGolemEntityRenderer {
    private static final Identifier CUSTOM_TEXTURE = Identifier.of(
            Just_let_me_do_it.MOD_ID, "textures/entity/copper_golem.png"
    );
    private static final Identifier CUSTOM_EYE_TEXTURE = Identifier.of(
            Just_let_me_do_it.MOD_ID, "textures/entity/copper_golem_eyes.png"
    );
    public CopperGolemEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.features.clear();
        this.addFeature(new EmissiveFeatureRenderer<>(
                this,
                state -> CUSTOM_EYE_TEXTURE,            
                (state, tickProgress) -> 1.0F,           
                new CopperGolemEntityModel(context.getPart(EntityModelLayers.COPPER_GOLEM)),
                RenderLayers::eyes,
                false
        ));
        this.addFeature(new net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer<>(this));
        CopperGolemEntityModel model = (CopperGolemEntityModel) this.model;
        this.addFeature(new net.minecraft.client.render.entity.feature.CopperGolemHeadBlockFeatureRenderer<>(
                this,
                state -> state.headBlockItemStack,
                model::transformMatricesForBlock
        ));
        this.addFeature(new net.minecraft.client.render.entity.feature.HeadFeatureRenderer<>(
                this,
                context.getEntityModels(),
                context.getPlayerSkinCache()
        ));
    }
    @Override
    public Identifier getTexture(CopperGolemEntityRenderState state) {
        return CUSTOM_TEXTURE;
    }
}
