package au.com.addstar.copycat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Util
{
	public static BlockFace rotateRight(BlockFace face)
	{
		switch(face)
		{
		case NORTH:
			return BlockFace.EAST;
		case EAST:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.WEST;
		default:
		case WEST:
			return BlockFace.NORTH;
		}
	}
	
	public static BlockFace getFacing(Player player)
	{
		float yaw = player.getLocation().getYaw();
		
		if(yaw <= -180)
			yaw += 360;

		if(yaw >= 180)
			yaw -= 360;

		if(yaw >= -45 && yaw <= 45)
			return BlockFace.SOUTH;
		else if(yaw > 45 && yaw < 135)
			return BlockFace.WEST;
		else if(yaw > -135 && yaw < -45)
			return BlockFace.EAST;
		else
			return BlockFace.NORTH;
	}
	
	public static float getYaw(BlockFace face)
	{
		switch(face)
		{
		case NORTH:
			return 180;
		case EAST:
			return -90;
		case SOUTH:
			return 0;
		default:
			return 90;
		}
	}
	
	public static String getTimeRemainString(long time)
	{
		time = Math.round(time / 1000f) * 1000;
		long minutes = 0;
		
		StringBuilder text = new StringBuilder();
		if(time > TimeUnit.MINUTES.toMillis(1))
		{
			if(text.length() != 0)
				text.append(" ");
			
			long value = time / TimeUnit.MINUTES.toMillis(1);
			minutes = value;
			text.append(value);
			text.append(" ");
			if(value != 1)
				text.append("Minutes");
			else
				text.append("Minute");
			time -= (value * TimeUnit.MINUTES.toMillis(1));
		}
		
		if(minutes < 1 || time >= 1000)
		{
			if(text.length() != 0)
				text.append(" ");
			
			long value = time / TimeUnit.SECONDS.toMillis(1);
			text.append(value);
			text.append(" ");
			if(value != 1)
				text.append("Seconds");
			else
				text.append("Second");
			time -= (value * TimeUnit.SECONDS.toMillis(1));
		}
		
		return text.toString();
	}
	
	public static List<String> matchString(String str, Collection<String> possibilities)
	{
		ArrayList<String> matches = new ArrayList<>();
		str = str.toLowerCase();
		
		for(String possible : possibilities)
		{
			if(possible.toLowerCase().startsWith(str))
				matches.add(possible);
		}
		
		return matches;
	}
}
