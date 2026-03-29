package com.frqme.just_let_me_do_it.client;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import java.util.concurrent.CompletableFuture;
public class Just_let_me_do_itDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(EnglishLangProvider::new);
    }
    private static class EnglishLangProvider extends FabricLanguageProvider {
        protected EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }
        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
            builder.add("item.just_let_me_do_it.copper_golem_core", "Copper Golem Core");
            builder.add("entity.just_let_me_do_it.copper_golem", "Copper Golem");
            builder.add("itemGroup.just_let_me_do_it.tab", "Just Let Me Do It");
        }
    }
}
