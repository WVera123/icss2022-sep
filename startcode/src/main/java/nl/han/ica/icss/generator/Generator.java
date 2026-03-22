package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

public class Generator {

	private StringBuilder cssBuilder;
	private static final String INDENT = "  ";

	public String generate(AST ast) {
		cssBuilder = new StringBuilder();

		generateStylesheet(ast.root);

		return cssBuilder.toString();
	}
	private void generateStylesheet(Stylesheet stylesheet) {
		for (ASTNode child : stylesheet.getChildren()) {
			if (child instanceof Stylerule) {
				generateStylerule((Stylerule) child);
			}
		}
	}

	private void generateStylerule(Stylerule rule) {
		String selectorName = rule.selectors.get(0).toString();
		cssBuilder.append(selectorName).append(" {\n");

		for (ASTNode child : rule.body) {
			if (child instanceof Declaration) {
				generateDeclaration((Declaration) child);
			}
		}
		cssBuilder.append("}\n\n");
	}

	private void generateDeclaration(Declaration decl) {
		cssBuilder.append(INDENT); // Indents (GE02)
		// EG: width: 100px;
		cssBuilder.append(decl.property.name)
				.append(": ")
				.append(getLiteralText((Literal) decl.expression))
				.append(";\n");
	}

	private String getLiteralText(Literal literal) {
		if (literal instanceof PixelLiteral) {
			return ((PixelLiteral) literal).value + "px";
		}
		else if (literal instanceof PercentageLiteral) {
			return ((PercentageLiteral) literal).value + "%";
		}
		else if (literal instanceof ColorLiteral) {
			return ((ColorLiteral) literal).value;
		}
		else if (literal instanceof ScalarLiteral) {
			return String.valueOf(((ScalarLiteral) literal).value);
		}
		return "";
	}
}