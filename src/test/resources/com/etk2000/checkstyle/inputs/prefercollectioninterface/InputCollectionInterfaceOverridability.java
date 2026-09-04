package com.etk2000.checkstyle.inputs.prefercollectioninterface;

public class InputCollectionInterfaceOverridability {
	public enum PublicEnum {
		CONSTANT {
			record InConstant(int value) {}

			public String onEnumConstant() {
				return null;
			}
		};

		public static class NestedInEnum {
			public String nestedInEnum() {
				return null;
			}
		}

		public String onEnum() {
			return null;
		}
	}

	public final class PublicFinal {
		public String onFinalOwner() {
			return null;
		}
	}

	public interface PublicIface {
		String onInterface();
	}

	public record PublicRecord(int value) {
		public String onRecord() {
			return null;
		}
	}

	public interface RecordHolder {
		record InInterface(int value) {}
	}

	private record PrivateRecord(int value) {}

	public static final class FinalOuter {
		public static class NestedInFinal {
			public String nestedInFinal() {
				return null;
			}
		}
	}

	static class PackagePrivate {
		public String onPackagePrivateOwner() {
			return null;
		}
	}

	private static class PrivateNested {
		public String onPrivateNestedOwner() {
			return null;
		}
	}

	public static sealed class Sealed permits SealedLeaf {
		public String onSealedOwner() {
			return null;
		}
	}

	public static final class SealedLeaf extends Sealed {
	}

	public static class WithPrivateConstructorsOnly {
		private WithPrivateConstructorsOnly() {
		}

		public String withPrivateConstructorsOnly() {
			return null;
		}
	}

	public static final Runnable ANONYMOUS = new Runnable() {
		public String onAnonymous() {
			return null;
		}

		@Override
		public void run() {
		}
	};

	public static String staticMethod() {
		return null;
	}

	public InputCollectionInterfaceOverridability() {
	}

	public final String finalMethod() {
		return null;
	}

	void localOwner() {
		class Local {
			public String onLocal() {
				return null;
			}
		}

		System.out.println(new Local().onLocal());
	}

	String packagePrivateMethod() {
		return null;
	}

	private String privateMethod() {
		return null;
	}

	public String publicMethod() {
		return null;
	}

	void recordOwners() {
		record InMethod(int value) {}

		final Runnable held = new Runnable() {
			record InAnonymous(int value) {}

			@Override
			public void run() {
			}
		};
		System.out.println(new InMethod(1) + held.toString());
	}
}