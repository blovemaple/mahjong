package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.utils.LanguageManager.*;

import java.util.HashMap;
import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliLabel;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTile extends CliLabel {
	public static final CliLabel TILE_BACK_FULL = new CliLabel("▇▇");
	public static final CliLabel TILE_BACK_HALF = new CliLabel("▄▄");
	public static final CliLabel TILE_PLACEHOLDER = new CliLabel("  ");

	private static final Map<TileType, CliTile> INSTANCES = new HashMap<>();

	public static CliTile of(Tile tile) {
		CliTile i = INSTANCES.get(tile.type());
		if (i == null)
			INSTANCES.put(tile.type(), i = new CliTile(tile.type()));
		return i;
	}

	@SuppressWarnings("unused")
	private TileType type;

	private CliTile(TileType type) {
		super(str(type.rank()) + (type.isNumberRank() ? str(type.suit()) : ""));
		this.type = type;
	}

}
