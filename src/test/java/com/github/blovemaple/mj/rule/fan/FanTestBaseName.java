/**
 * 
 */
package com.github.blovemaple.mj.rule.fan;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 在FanTest上标记跑test的base name。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface FanTestBaseName {
	/**
	 * @return base name
	 */
	String value();
}
