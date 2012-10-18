package blove.mj;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 玩家。
 * 
 * @author blovemaple
 */
public class Player {
	private static AtomicInteger nextId = new AtomicInteger();

	private int id = nextId.getAndIncrement();
	private final String name;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            名字
	 */
	public Player(String name) {
		this.name = name;
	}

	/**
	 * 返回名字。
	 * 
	 * @return 名字
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
