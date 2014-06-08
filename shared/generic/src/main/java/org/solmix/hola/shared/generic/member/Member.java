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
package org.solmix.hola.shared.generic.member;

import org.solmix.hola.core.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月17日
 */

public class Member implements Comparable<Member>
{
    ID member;
    Object data;

    public Member(ID member) {
          this(member, null);
    }

    public Member(ID member, Object data) {
          this.member = member;
          this.data = data;
    }

    @Override
    public boolean equals(Object o) {
          if (o != null && o instanceof Member) {
                return member.equals(((Member) o).member);
          }
          return false;
    }

    @Override
    public int hashCode() {
          return member.hashCode();
    }

    @Override
    public int compareTo(Member o) {
          if (o != null && o instanceof Member) {
                return member.compareTo(o.member);
          }
          return 0;
    }

    public ID getID() {
          return member;
    }

    public Object getData() {
          return data;
    }

    @Override
    public String toString() {
          final StringBuffer sb = new StringBuffer();
          sb.append("Member[").append(member).append(";").append(data) 
                      .append("]"); 
          return sb.toString();
    }

}
