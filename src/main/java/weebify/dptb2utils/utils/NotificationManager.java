package weebify.dptb2utils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.Notification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NotificationManager {
    private static final ResourceLocation BG = new ResourceLocation(DPTB2Utils.MOD_ID, "textures/advancement.png");
    private static final ResourceLocation ICON_DEFAULT = new ResourceLocation(DPTB2Utils.MOD_ID, "textures/notif.png");

    private final List<Notification> visible = new LinkedList<>();
    private final Queue<Notification> queue = new LinkedList<>();

    private static final int MAX_VISIBLE = 5;

    private static final NotificationManager instance = new NotificationManager();
    public static NotificationManager getInstance() {
        return instance;
    }

    public void add(String title, String desc, int color, String soundName) {
        Notification n = new Notification(title, desc, color, ICON_DEFAULT, soundName);
        synchronized (visible) {
            if (visible.size() < MAX_VISIBLE) {
                visible.add(n);
            } else {
                queue.add(n);
            }
        }
    }

    public void add(String title, String desc, int color, String soundName, float volume, float pitch) {
        Notification n = new Notification(title, desc, color, ICON_DEFAULT, soundName, pitch, volume);
        synchronized (visible) {
            if (visible.size() < MAX_VISIBLE) {
                visible.add(n);
            } else {
                queue.add(n);
            }
        }
    }


    public void render(ScaledResolution res) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        Gui gui = mc.ingameGUI;

        synchronized (visible) {
            // remove expired from visible
            Iterator<Notification> it = visible.iterator();
            boolean removed = false;
            while (it.hasNext()) {
                if (it.next().isExpired()) {
                    it.remove();
                    removed = true;
                }
            }

            while (visible.size() < MAX_VISIBLE && !queue.isEmpty()) {
                visible.add(queue.poll());
            }

            int xRight = res.getScaledWidth();
            int y = 0;
            int margin = 0;
            for (Notification n : visible) {
                int toastWidth = Notification.TOAST_WIDTH;
                int toastHeight = n.getHeight();
                int x = xRight - toastWidth - margin;
                mc.getTextureManager().bindTexture(BG);
                n.draw(gui, fr, x, y);
                y += toastHeight;
            }
        }
    }
}
