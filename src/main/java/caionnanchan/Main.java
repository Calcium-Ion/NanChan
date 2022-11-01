package caionnanchan;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.BreedEvent;
import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

@Mod(modid = "nanchan", name = "Nan Chan", acceptableRemoteVersions = "*", dependencies = "required-after:pixelmon")
public class Main {

    final String path = "./config/nanchan";
    int hatchChance = 50;
    int breedChance = 50;

    @Mod.EventHandler
    public void onStart(FMLPreInitializationEvent event){
        Pixelmon.EVENT_BUS.register(this);
        Sponge.getEventManager().registerListeners(this,this);
        load();
    }

    private void load(){
        Properties properties = new Properties();
        File pathF = new File(path);
        File config = new File(pathF,"config.cfg");
        try {
            if(!pathF.exists()) pathF.mkdirs();
            if(!config.exists()) config.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(config);
            properties.load(fileInputStream);
            if(properties.get("hatchChance") == null) properties.setProperty("hatchChance", "50");
            if(properties.get("breedChance") == null) properties.setProperty("breedChance", "50");
            hatchChance = Integer.parseInt(properties.getProperty("hatchChance"));
            breedChance = Integer.parseInt(properties.getProperty("breedChance"));
            properties.store(new FileOutputStream(config),"hatchChance 0-Ivs，breedChance no-Egg");
        } catch (IOException e) {
            System.out.println("配置文件加载失败");
            e.printStackTrace();
        }
    }

    @Listener
    public void onReload(GameReloadEvent event){
        load();
    }

    @SubscribeEvent
    public void onRanch(EggHatchEvent event){
        UUID uuid = event.pokemon.getOwnerPlayerUUID();
        if (uuid != null){
            Optional<User> user = getUser(uuid);
            if(!user.get().hasPermission("nanchan.baohu")){
                int rand = new Random().nextInt(101);
                if(rand < hatchChance){
                    event.pokemon.getIVs().attack = 0;
                    event.pokemon.getIVs().defence = 0;
                    event.pokemon.getIVs().specialAttack = 0;
                    event.pokemon.getIVs().specialDefence = 0;
                    event.pokemon.getIVs().speed = 0;
                    event.pokemon.getIVs().hp = 0;
                    Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GREEN,
                            "系统: ",TextColors.WHITE,"玩家",user.get().getName(),"的",event.pokemon.getLocalizedName(),
                            "在孵化时被查出有脑萎缩个体全部变成了0"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreed(BreedEvent.MakeEgg event){
        UUID uuid = event.owner;
        if (uuid != null) {
            Optional<User> user = getUser(uuid);
            if (!user.get().hasPermission("nanchan.baohu")) {
                int rand = new Random().nextInt(101);
                if(rand < breedChance){
                    event.setCanceled(true);
                    Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.GREEN,"系统: ",TextColors.WHITE,"玩家",user.get().getName(),"的",event.getEgg().getLocalizedName(),
                            "在出生时难产了"));
                }
            }
        }
    }

    public static Optional<User> getUser(UUID uuid) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid);
    }
}
