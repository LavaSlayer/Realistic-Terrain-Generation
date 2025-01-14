package rtg.world.biome.realistic.ridiculousworld;

import rtg.api.biome.BiomeConfig;
import rtg.world.gen.surface.ridiculousworld.SurfaceRWBotanicalGarden;
import rtg.world.gen.terrain.ridiculousworld.TerrainRWBotanicalGarden;

import net.minecraft.world.biome.BiomeGenBase;

public class RealisticBiomeRWBotanicalGarden extends RealisticBiomeRWBase
{
    
    public RealisticBiomeRWBotanicalGarden(BiomeGenBase rwBiome, BiomeConfig config)
    {
    
        super(config,
            rwBiome,
            BiomeGenBase.river,
            new TerrainRWBotanicalGarden(),
            new SurfaceRWBotanicalGarden(config, rwBiome.topBlock, rwBiome.fillerBlock, false, null, 0f, 1.5f, 60f, 65f, 1.5f, rwBiome.topBlock, 0.05f));
    }
}
