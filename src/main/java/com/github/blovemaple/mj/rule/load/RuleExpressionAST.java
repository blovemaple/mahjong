package com.github.blovemaple.mj.rule.load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

@Data
public class RuleExpressionAST {
	public static enum ASTNodeType {
		LOGICAL_CONDITION, //
		SINGLE_CONDITION, //

		INCLUDING, //
		ALL_MATCHING, //
		EQUALLING, //
		MATCHING, //

		FUNCTION, //
		FUNCTION_NAME, //
		FUNCTION_PARAM, //

		ITEM_CONDITION, //
		SINGLE_ITEM_CONDITION, //

		UNIT_CONDITION, //
		UNIT_TYPE_CONDITION, //
		ANY_UNIT_TYPE, //
		UNIT_SOURCE_CONDITION, //
		TILE_TYPE_CONDITION, //
		SUIT_CONDITION, //
		SUIT_CONDITION_CONTENT, //
		SINGLE_SUIT_CONDITION_CONTENT, //
		RANK_CONDITION, //
		RANK_CONDITION_CONTENT, //
		SINGLE_RANK_CONDITION_CONTENT, //

		ITEM, //
		UNIT, //
		UNIT_TYPE, //
		UNIT_SOURCE, //
		TILE_TYPE, //
		SUIT, //
		SUIT_CONTENT, //
		RANK, //
		RANK_CONTENT, //

		MULTI_VAR, //
		VAR, //
		VAR_OFFSET, //

		MULTI_FORM, //
		ALL_FORM, //
		ANY_FORM, //

	}

	@Data
	public static class ASTNode {
		private ASTNodeType type;
		private String name;
		private List<ASTNode> children;

		public ASTNode(ASTNodeType type) {
			this(type, (String) null);
		}

		public ASTNode(ASTNodeType type, ASTNode... children) {
			this(type, null, children);
		}

		public ASTNode(ASTNodeType type, String name, ASTNode... children) {
			Objects.requireNonNull(type);

			this.type = type;
			this.name = name;
			if (children != null && children.length > 0)
				this.children = new ArrayList<>(Arrays.asList(children));
		}

		public void addChild(ASTNode child) {
			if (children == null)
				children = new ArrayList<ASTNode>();
			children.add(child);
		}

	}

	@NonNull
	private ASTNode root;

}
