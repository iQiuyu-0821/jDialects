/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

/**
 * Function features Utils
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class FunctionUtils {//NOSONAR

	/**
	 * The render method translate function template to real SQL piece
	 * 
	 * <pre>
	 * Template can be:
	 * "*": standard SQL function, identical to abc($Params)
	 * "abc($Params)": template with special parameter format:  
	   				"$P1, $P2, $P3, $P4, $P5, $P6..."="$Params"
					"$P1,$P2,$P3,$P4,$P5,$P6..."="$Compact_Params"
					"$P1||$P2||$P3||$P4||$P5||$P6..."="$Lined_Params"
					"$P1+$P2+$P3+$P4+$P5+$P6..."="$Add_Params");
					"$P1 in $P2 in $P3 in $P4 in $P5 in $P6..."="$IN_Params"
			        "$P1%pattern$P2%pattern$P3%pattern$P4%pattern$P5%pattern$P6..."="$Pattern_Params"
					"11%startswith$P2%startswith$P3%startswith$P4%startswith$P5%startswith$P6..."= "$Startswith_Params");
					 "nvl($P1, nvl($P2, nvl($P3, nvl($P4, nvl($P5, $P6...)))))"="$NVL_Params");
	 * 
	 * "0=abc()": function do not support parameter
	 * "1=abc($P1)": function only support 1 parameter
	 * "2=abc($P1,$P2)": function only support 2 parameters
	 * "0=abc()|1=abc($P1)|3=abc($P1,$P2,$P3)": function support 0 or 1 or 3 parameters
	 * 
	 * </pre>
	 * 
	 * @param functionName function name
	 * @param args function parameters
	 * @return A SQL function piece
	 */
	protected static String render(Dialect d, String functionName, Object... args) {
		String template = d.functions.get(functionName);
		DialectException.assureNotEmpty(template, "Dialect \"" + d + "\" does not support \"" + functionName
				+ "\" function, a full list of supported functions of this dialect can see \"DatabaseDialects.xls\"");
		if ("*".equals(template))
			template = functionName + "($Params)";
		char c = template.charAt(1);
		if (c != '=') {
			if (template.indexOf("$Params") >= 0) {
				return StrUtils.replace(template, "$Params", StrUtils.arrayToString(args, ", "));
			}
			if (template.indexOf("$Compact_Params") >= 0) {
				return StrUtils.replace(template, "$Compact_Params", StrUtils.arrayToString(args, ","));
			}
			if (template.indexOf("$Lined_Params") >= 0) {
				return StrUtils.replace(template, "$Lined_Params", StrUtils.arrayToString(args, "||"));
			}
			if (template.indexOf("$Add_Params") >= 0) {
				return StrUtils.replace(template, "$Add_Params", StrUtils.arrayToString(args, "+"));
			}
			if (template.indexOf("$IN_Params") >= 0) {
				return StrUtils.replace(template, "$IN_Params", StrUtils.arrayToString(args, " in "));
			}
			if (template.indexOf("$Pattern_Params") >= 0) {
				return StrUtils.replace(template, "$Pattern_Params", StrUtils.arrayToString(args, "%pattern"));
			}
			if (template.indexOf("$Startswith_Params") >= 0) {
				return StrUtils.replace(template, "$Startswith_Params", StrUtils.arrayToString(args, "%startswith"));
			}
			if (template.indexOf("$NVL_Params") >= 0) {
				if (args == null || args.length < 2)
					DialectException.throwEX("Nvl function require at least 2 parameters");
				else {
					String s = "nvl(" + args[args.length - 2] + ", " + args[args.length - 1] + ")";
					for (int i = args.length - 3; i > -1; i--)
						s = "nvl(" + args[i] + ", " + s + ")";
					return StrUtils.replace(template, "$NVL_Params", s);
				}
			}
			return (String) DialectException.throwEX("jDialect found a template bug error, please submit this bug");
		} else {
			int argsCount = 0;
			if (args != null)
				argsCount = args.length;
			String searchStr = Integer.toString(argsCount) + "=";
			if (template.indexOf(searchStr) < 0)
				DialectException.throwEX("Dialect " + d + "'s function \"" + functionName + "\" only support "
						+ allowedParameterCounts(template) + " parameters");
			String result = StrUtils.substringBetween(template + "|", searchStr, "|");
			for (int i = 0; args != null && i < args.length; i++) {
				result = StrUtils.replace(result, "$P" + (i + 1), "" + args[i]);
			}
			return result;
		}
	}

	private static String allowedParameterCounts(String template) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			if (template.indexOf(Integer.toString(i) + "=") >= 0)
				sb.append(Integer.toString(i)).append(" or ");
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 4);
		return sb.toString();
	}
}
