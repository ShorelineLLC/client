package net.shoreline.server.route.loader;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.loader.classcache.ClassCache;
import net.shoreline.server.route.loader.routes.*;
import net.shoreline.server.token.TokenManager;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public final class LoaderEndpoint implements EndpointGroup
{
    // public final ClassCache RELEASE_CLASS_CACHE;
    public static ClassCache BETA_CLASS_CACHE;
    public static ClassCache DEV_CLASS_CACHE;

    public static TokenManager DEV_TOKEN_MANAGER = new TokenManager(5000L);
    public static TokenManager BETA_TOKEN_MANAGER = new TokenManager(5000L);
    public static TokenManager RELEASE_TOKEN_MANAGER = new TokenManager(5000L);

    public static TokenManager IRC_TOKEN_MANAGER = new TokenManager(300000L);

    public static ConcurrentHashMap<String, IRCSession> awaitingTokens = new ConcurrentHashMap<>();

    static
    {
        ServerMain.LOGGER.info("Caching class information from the client jars");

//        File releaseJar = new File("/home/container/assets/loader/release/client.jar");
//        RELEASE_CLASS_CACHE = new ClassCache(releaseJar);

        File betaJar = new File("/home/container/assets/loader/beta/client.jar");
        BETA_CLASS_CACHE = new ClassCache(betaJar);

        File devJar = new File("/home/container/assets/loader/dev/client.jar");
        DEV_CLASS_CACHE = new ClassCache(devJar);

        ServerMain.LOGGER.info("Successfully created the class cache");
    }

    @Override
    public void addEndpoints()
    {
        ApiBuilder.post("login", new LoginRoute());
        ApiBuilder.get("natives", new NativesRoute());
        ApiBuilder.get("versioncheck", new VersionCheckRoute());
        ApiBuilder.get("update", new AutoUpdateRoute());
        ApiBuilder.get("gentoken", new GenTokenRoute());
        ApiBuilder.get("loadresources", new LoadResourcesRoute());
    }

    public static class IRCSession
    {
        private final String uid;
        private final String username;
        private final String usertype;
        private String maskedUserType;
        private boolean chatEnabled;

        public IRCSession(String uid,
                          String username,
                          String usertype,
                          boolean chatEnabled)
        {
            this.uid = uid;
            this.username = username;
            this.usertype = usertype;
            this.maskedUserType = usertype;
            this.chatEnabled = chatEnabled;
        }

        public String getUID()
        {
            return this.uid;
        }

        public String getUsername()
        {
            return this.username;
        }

        public String getUsertype()
        {
            return this.usertype;
        }

        public String getMaskedUserType()
        {
            return this.maskedUserType;
        }

        public void setMaskedUserType(String usertype)
        {
            this.maskedUserType = usertype;
        }

        public boolean isChatEnabled()
        {
            return this.chatEnabled;
        }

        public void setChatEnabled(boolean chatEnabled)
        {
            this.chatEnabled = chatEnabled;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof IRCSession session))
            {
                return false;
            }

            return session.username.equals(this.username) && session.uid.equals(this.uid) && session.usertype.equals(this.usertype);
        }
    }
}
