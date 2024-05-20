package net.shoreline.client.impl.manager;

import net.shoreline.client.Shoreline;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.SkipRegister;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.module.client.BaritoneModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.ReflectionUtil;
import net.shoreline.client.util.StreamUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xgraza
 * @since 05/19/24
 */
@SuppressWarnings("unchecked")
public final class ModuleManager
{
    private static final String MODULE_IMPL = "net.shoreline.client.impl.module";

    private final Map<Class<? extends Module>, Module> moduleInstanceMap = new LinkedHashMap<>();
    private final Map<String, Module> moduleIdInstanceMap = new HashMap<>();
    private List<Module> moduleList = new LinkedList<>();

    public ModuleManager()
    {
        try
        {
            ReflectionUtil.reflectInPackage(MODULE_IMPL).forEach((moduleClass) ->
            {
                // Make sure the class we're reflecting is a module class
                if (!Module.class.isAssignableFrom(moduleClass))
                {
                    return;
                }

                // Some modules may be completely broken or not finished or are inside jokes
                // Do not register these.
                if (moduleClass.isAnnotationPresent(SkipRegister.class))
                {
                    Shoreline.LOGGER.debug("@SkipRegister on {}, skipping", moduleClass);
                    return;
                }

                if (BaritoneModule.class.isAssignableFrom(moduleClass) && !ShorelineMod.isBaritonePresent())
                {
                    Shoreline.info("Baritone module not supported - Baritone not in mod path!");
                    return;
                }

                try
                {
                    // Invoke constructor & register instance created
                    register((Module) moduleClass.getConstructors()[0].newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    Shoreline.error("Failed to register class {}", moduleClass);
                    e.printStackTrace();
                }
            });
        } catch (final IOException | ClassNotFoundException e)
        {
            Shoreline.error("Failed to reflect module classes.");
            // Throwing here is intentional, we want this to fail
            throw new RuntimeException(e);
        }
        Shoreline.info("Reflected {} modules", moduleList.size());
        moduleList = StreamUtils.sortCached(moduleList.stream(), Module::getName).collect(Collectors.toList());
    }

    public void register(final Module module)
    {
        // Cache module instance
        moduleInstanceMap.put(module.getClass(), module);
        moduleIdInstanceMap.put(module.getId(), module);
        moduleList.add(module);

        // Automatically register settings & macro
        module.reflectConfigs();
        if (module instanceof ToggleModule toggleModule) {
            Managers.MACRO.register(toggleModule.getKeybinding());
        }

        // Automatically reflect the INSTANCE variable if it exists
        try
        {
            // Set the static INSTANCE variable to the local instance we created
            module.getClass().getDeclaredField("INSTANCE").set(module, module);
        } catch (final NoSuchFieldException | IllegalAccessException e)
        {
            // No .INSTANCE found
        }
    }

    public void register(final Module... modules)
    {
        for (final Module module : modules)
        {
            register(module);
        }
    }

    public <T extends Module> T getModuleById(final String id)
    {
        return (T) moduleIdInstanceMap.get(id);
    }

    public List<Module> getModules()
    {
        return moduleList;
    }
}
