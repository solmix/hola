/*
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.hola.shell;

import static java.lang.System.out;

import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.karaf.shell.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.identity.IDFactory;
import org.solmix.hola.core.identity.Namespace;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年6月8日
 */
@Command(scope = "namespace", name = "list", description = "List namespace in osig container")

public class NamespaceCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceCommand.class);

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        List<Namespace> ns=  IDFactory.getDefault().getNamespaces();
        for(Namespace n:ns){
            out.println(n.getClass().getName());
            out.println(ShellUtil.getUnderlineString(n.getClass().getName()));
            out.println("Name:        "+n.getName());
            out.println("Description: "+n.getDescription());
            out.println("Scheme:      "+n.getScheme());
            out.println("\n");
        }
        return null;
    }

}
