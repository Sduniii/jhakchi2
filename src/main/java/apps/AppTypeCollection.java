package apps;

import Properties.Resources;
import javafx.scene.image.Image;


public class AppTypeCollection {
    //public delegate NesMiniApplication

    public static class AppInfo {
        public Class clazz;
        public String[] extensions;
        public String[] defaultApps;
        public char prefix;
        public Image defaultCover;
    }

    public static AppInfo[] ApplicationTypes = new AppInfo[]
            {
                    new AppInfo() {{
                        clazz = NesGame.class;
                        extensions = new String[]{".nes"};
                        defaultApps = new String[]{"/bin/nes", "/bin/clover-kachikachi-wr", "/usr/bin/clover-kachikachi"};
                        prefix = 'H';
                        defaultCover = Resources.blank_nes;
                    }},
                    new AppInfo() {{
                        clazz = NesUGame.class;
                        extensions = new String[]{".unf", ".unif", ".nes", ".fds"};
                        defaultApps = new String[]{"/bin/nes"};
                        prefix = 'I';
                        defaultCover = Resources.blank_jp;
                    }},
                    new AppInfo() {{
                        clazz = FdsGame.class;
                        extensions = new String[]{".fds"};
                        defaultApps = new String[]{"/bin/nes", "/bin/clover-kachikachi-wr", "/usr/bin/clover-kachikachi"};
                        prefix = 'D';
                        defaultCover = Resources.blank_fds;
                    }},
                    new AppInfo() {{
                        clazz = N64Game.class;
                        extensions = new String[]{".sfc", ".smc", ".sfrom"};
                        defaultApps = new String[]{"/bin/snes", "/bin/clover-canoe-shvc-wr", "/usr/bin/clover-canoe-shvc"};
                        prefix = 'U';
                        defaultCover = Resources.blank_snes_us;
                    }},
                    new AppInfo() {{
                        clazz = SnesGame.class;
                        extensions = new String[]{".n64", ".z64", ".v64"};
                        defaultApps = new String[]{"/bin/n64"};
                        prefix = '6';
                        defaultCover = Resources.blank_n64;
                    }},
                    new AppInfo() {{
                        clazz = SmsGame.class;
                        extensions = new String[]{".sms"};
                        defaultApps = new String[]{"/bin/sms"};
                        prefix = 'M';
                        defaultCover = Resources.blank_sms;
                    }},
                    new AppInfo() {{
                        clazz = GenesisGame.class;
                        extensions = new String[]{".gen", ".md", ".smd"};
                        defaultApps = new String[]{"/bin/md"};
                        prefix = 'G';
                        defaultCover = Resources.blank_genesis;
                    }},
                    new AppInfo() {{
                        clazz = Sega32XGame.class;
                        extensions = new String[]{".32x"};
                        defaultApps = new String[]{"/bin/32x"};
                        prefix = 'B';
                        defaultCover = Resources.blank_32x;
                    }},
                    new AppInfo() {{
                        clazz = GbGame.class;
                        extensions = new String[]{".gb"};
                        defaultApps = new String[]{"/bin/gb"};
                        prefix = 'B';
                        defaultCover = Resources.blank_gb;
                    }},
                    new AppInfo() {{
                        clazz = GbcGame.class;
                        extensions = new String[]{".gbc"};
                        defaultApps = new String[]{"/bin/gbc"};
                        prefix = 'C';
                        defaultCover = Resources.blank_gbc;
                    }},
                    new AppInfo() {{
                        clazz = GbaGame.class;
                        extensions = new String[]{".gba"};
                        defaultApps = new String[]{"/bin/gba"};
                        prefix = 'A';
                        defaultCover = Resources.blank_gba;
                    }},
                    new AppInfo() {{
                        clazz = PceGame.class;
                        extensions = new String[]{".pce"};
                        defaultApps = new String[]{"/bin/pce"};
                        prefix = 'E';
                        defaultCover = Resources.blank_pce;
                    }},
                    new AppInfo() {{
                        clazz = GameGearGame.class;
                        extensions = new String[]{".gg"};
                        defaultApps = new String[]{"/bin/gg"};
                        prefix = 'R';
                        defaultCover = Resources.blank_gg;
                    }},
                    new AppInfo() {{
                        clazz = Atari2600Game.class;
                        extensions = new String[]{".a26"};
                        defaultApps = new String[]{"/bin/a26"};
                        prefix = 'T';
                        defaultCover = Resources.blank_2600;
                    }},
                    new AppInfo() {{
                        clazz = ArcadeGame.class;
                        extensions = new String[]{};
                        defaultApps = new String[]{"/bin/fba", "/bin/mame", "/bin/cps2", "/bin/neogeo"};
                        prefix = 'X';
                        defaultCover = Resources.blank_arcade;
                    }}
            };

    public static AppInfo GetAppByExtension(String extension) {
        for (AppInfo app : ApplicationTypes) {
            for (String s : app.extensions)
                if (extension.equalsIgnoreCase(s)) return app;
        }
        return null;
    }

    public static AppInfo GetAppByExec(String exec) {
        exec = exec.replaceAll("['\\\"]|(\\.7z)", " ");
        exec += " ";
        for (AppInfo app : ApplicationTypes)
            for (String cmd : app.defaultApps)
                if (exec.startsWith(cmd + " ")) {
                    if (app.extensions.length == 0)
                        return app;
                    for (String ext : app.extensions) {
                        if (exec.contains(ext + " "))
                            return app;
                    }
                }
        return null;
    }
}
