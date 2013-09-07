package com.github.Jikoo.BookSuite.copy;

public enum CopyFailureReason {
	PERMISSION, SPACE, SUPPLIES_ALL, SUPPLIES_INK, SUPPLIES_MAP, SUPPLIES_PRINTABLE, UNCOPIABLE;

	public String getFailureReason() {
		switch (this) {
		case PERMISSION:
			return "You do not have permission to copy that!";
		case SPACE:
			return "You do not have enough space to make a copy!";
		case SUPPLIES_ALL:
			return "You need ink and a blank book to make a copy!";
		case SUPPLIES_INK:
			return "You need ink to make a copy!";
		case SUPPLIES_MAP:
			return "You need 9 paper to make a copy!";
		case SUPPLIES_PRINTABLE:
			return "You need a book or 3 paper and 1 leather to make a copy!";
		case UNCOPIABLE:
			return "That's not copiable!";
		default:
			return "It should not be possible to get this message."
					+ " If you see it, what are you even doing? Seriously, please tell me.";
		}
	}
}
