/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

 
/**
 * A shortcut annotation of FKey, only for one column
 */
@Target(FIELD) 
@Retention(RUNTIME)
public @interface SingleFKey {
    /**
     * (Optional) The name of the foreign key. 
     */
    String name() default "";
 
	/**
	 * Referenced table name and columns, first is table name, followed by column
	 * names, like "table1, col1, col2..."
	 */
	String[] refs() default {};
}
