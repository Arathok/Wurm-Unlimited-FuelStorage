package org.arathok.wurmunlimited.mods.fuelstorage;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

public class test
{
    HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.MethodsStructure", "hasEnoughSkillToExpandStructure", "(Lcom/wurmonline/server/creatures/Creature;IILcom/wurmonline/server/structures/Structure;)Z", new InvocationHandlerFactory()

        {
    }
}

