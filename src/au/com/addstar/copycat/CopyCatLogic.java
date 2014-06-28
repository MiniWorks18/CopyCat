package au.com.addstar.copycat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import au.com.addstar.monolith.MonoPlayer;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.events.EndMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.StartMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreTypeBase;

public class CopyCatLogic extends ScoreTypeBase
{
	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame minigame )
	{
		System.out.println("Balancing team");
	}

	@Override
	public String getType()
	{
		return "CopyCat";
	}
	
	@EventHandler
	public void onMinigameJoin(final JoinMinigameEvent event)
	{
		if(event.isCancelled())
			return;
		
		final GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
		{
			List<String> errors = board.getErrors();
			if(!errors.isEmpty())
			{
				for(String error : errors)
					event.getPlayer().sendMessage(ChatColor.RED + error);

				event.setCancelled(true);
			}
			
			Bukkit.getScheduler().runTask(CopyCatPlugin.instance, new Runnable()
			{
				@Override
				public void run()
				{
					if(event.getMinigamePlayer().isInMinigame())
						MonoPlayer.getPlayer(event.getPlayer()).setBossBarDisplay(board.getBossDisplay());
				}
			});
		}
	}
	
	@EventHandler
	public void onMinigameStart(StartMinigameEvent event)
	{
		GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
			board.beginGame();
	}
	
	@EventHandler
	public void onMinigameQuit(QuitMinigameEvent event)
	{
		GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
		{
			if(!event.isForced())
				board.onPlayerLeave(event.getMinigamePlayer());
			
			MonoPlayer.getPlayer(event.getPlayer()).setBossBarDisplay(null);
		}
	}
	
	@EventHandler
	public void onMinigameEnd(EndMinigameEvent event)
	{
		GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
		{
			for(MinigamePlayer player : event.getWinners())
				MonoPlayer.getPlayer(player.getPlayer()).setBossBarDisplay(null);
		}
	}

	private GameBoard getBoard(MinigamePlayer player)
	{
		if(player == null || !player.isInMinigame())
			return null;
			
		return CopyCatPlugin.instance.getBoardByGame(player.getMinigame().getName(false));
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		
		GameBoard board = getBoard(player);
		if(board != null)
		{
			if(!board.canModify(player, event.getBlock().getLocation()))
				event.setCancelled(true);
			else
				board.onPlaceBlock(player);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockBreak(BlockBreakEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		
		GameBoard board = getBoard(player);
		if(board != null)
		{
			if(!board.canModify(player, event.getBlock().getLocation()))
				event.setCancelled(true);
		}
	}
}
