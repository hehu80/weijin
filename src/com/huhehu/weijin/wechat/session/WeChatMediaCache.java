/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.huhehu.weijin.wechat.session;

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import static com.huhehu.weijin.wechat.conversation.WeChatMessage.TYPE_FILE;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 *
 * @author henning
 */
public class WeChatMediaCache {

    protected static final String URL_AVATAR_DOWNLOAD = "https://web.wechat.com%s";
    protected static final String URL_MESSAGE_IMAGE_DOWNLOAD = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxgetmsgimg?&MsgID=%s&skey=%s&type=big";
    private static final String MEDIA_DIRECTORY = "media";

    private WeChatSession session;
    private Map<String, Image> images = new HashMap<>();
    private ExecutorService mediaDownloader;

    protected WeChatMediaCache(WeChatSession session) {
        this.session = session;

        mediaDownloader = Executors.newFixedThreadPool(16, new WeChatSessionThreadFactory("WeChat-Media"));
    }

    public WeChatMediaCache clearAll() {
        images.clear();
        return this;
    }

    public WeChatMediaCache loadAll() {
        mediaDownloader.submit(() -> {
            try {
                Files.list(Paths.get(MEDIA_DIRECTORY)).forEach((file) -> {
                    if (Files.isRegularFile(file) && Files.isReadable(file)) {
                        try {
                            Image image = ImageIO.read(file.toFile());
                            synchronized (images) {
                                images.put(file.getFileName().toString(), image);
                            }
                        } catch (IOException ignore) {

                        }
                    }
                });
            } catch (IOException ignore) {
            }
        });
        return this;
    }

    protected void shutdownNow() {
        mediaDownloader.shutdownNow();
    }

    public WeChatMediaCache downloadMedia(WeChatContact contact, boolean refresh, Runnable onDownloaded) {
        return downloadMedia(getContactAvatarMediaId(contact), refresh, true,
                String.format(URL_AVATAR_DOWNLOAD, contact.getImageUrl()),
                onDownloaded);
    }

    public WeChatMediaCache downloadMedia(WeChatMessage message, boolean refresh, Runnable onDownloaded) {
        return downloadMedia(getMessageMediaId(message), refresh, true,
                String.format(URL_MESSAGE_IMAGE_DOWNLOAD, message.getId(), session.getConnection().getSessionKey()),
                onDownloaded);
    }

    protected WeChatMediaCache downloadMedia(String mediaId, boolean refresh, boolean useCache, String url, Runnable onDownloaded) {
        if (refresh || !images.containsKey(mediaId)) {
            mediaDownloader.submit(() -> {
                boolean fromCache = images.containsKey(mediaId);
                Path mediaFile = Paths.get(MEDIA_DIRECTORY, mediaId);

                if (useCache && !fromCache && Files.exists(mediaFile)) {
                    try {
                        Image image = ImageIO.read(mediaFile.toFile());
                        synchronized (images) {
                            images.put(mediaId, image);
                        }
                        onDownloaded.run();
                        fromCache = true;
                    } catch (IOException ignore) {
                    }
                }

                if (refresh || !fromCache) {
                    try (InputStream inputStream = session.getConnection().openConnection(url).getInputStream()) {
                        System.out.println("DOWNLOAD " + mediaId);
                        if (!Files.exists(Paths.get(MEDIA_DIRECTORY))) {
                            Files.createDirectory(Paths.get(MEDIA_DIRECTORY));
                        }
                        Files.copy(inputStream, mediaFile, StandardCopyOption.REPLACE_EXISTING); // TODO in byte array
                    } catch (IOException ignore) {
                    }

                    try {
                        Image image = ImageIO.read(mediaFile.toFile());
                        synchronized (images) {
                            images.put(mediaId, image);
                        }
                        onDownloaded.run();
                    } catch (IOException ignore) {
                    }
                }
            });
        }

        return this;
    }

    public boolean isMediaMessage(WeChatMessage message) {
        return message != null && message.getMsgType() == TYPE_FILE;
    }

    protected String getContactAvatarMediaId(WeChatContact contact) {
        String mediaId = contact.getImageUrl();
        int seqStart = mediaId.indexOf("seq=") + 4;
        int seqEnd = mediaId.indexOf("&", seqStart);
        return "avatar_" + mediaId.substring(seqStart, seqEnd);
    }

    protected String getMessageMediaId(WeChatMessage message) {
        return "media_" + message.getId();
    }

    public Image getMedia(WeChatContact contact) {
        return getMedia(getContactAvatarMediaId(contact));
    }

    public Image getMedia(WeChatMessage message) {
        return getMedia(getMessageMediaId(message));
    }

    protected Image getMedia(String mediaId) {
        synchronized (images) {
            if (images.containsKey(mediaId)) {
                return images.get(mediaId);
            } else {
                return null;
            }
        }
    }
}
