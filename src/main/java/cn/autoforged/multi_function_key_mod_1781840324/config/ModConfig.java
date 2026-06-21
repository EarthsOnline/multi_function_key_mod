package cn.autoforged.multi_function_key_mod_1781840324.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ModConfig {
    public static class Client {
        public final ModConfigSpec.ConfigValue<List<? extends String>> boundActions;
        public final ModConfigSpec.ConfigValue<String> mulkKeyName;

        Client(ModConfigSpec.Builder builder) {
            builder.push("general");
            mulkKeyName = builder
                    .comment("MULK key identifier. Default: key.keyboard.left.control")
                    .define("mulkKey", "key.keyboard.left.control");
            boundActions = builder
                    .comment("List of key mapping names bound to MULK")
                    .defineListAllowEmpty("boundActions", List.of(), s -> s instanceof String);
            builder.pop();
        }
    }

    public static final ModConfigSpec SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        SPEC = pair.getRight();
        CLIENT = pair.getLeft();
    }
}
