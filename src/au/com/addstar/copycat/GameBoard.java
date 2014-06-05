package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.copycat.flags.BooleanFlag;
import au.com.addstar.copycat.flags.EnumFlag;
import au.com.addstar.copycat.flags.Flag;
import au.com.addstar.copycat.flags.FlagIO;
import au.com.addstar.copycat.flags.Flaggable;
import au.com.addstar.copycat.flags.IntegerFlag;
import au.com.addstar.copycat.flags.StringFlag;
import au.com.addstar.copycat.logic.EliminationMainState;
import au.com.addstar.copycat.logic.PreRoundState;
import au.com.addstar.copycat.logic.ScoringMainState;
import au.com.addstar.copycat.logic.State;
import au.com.addstar.copycat.logic.StateEngine;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class GameBoard implements Flaggable
{
	public enum GameMode
	{
		Elimination,
		Score
	}
	
	private PlayerStation[] mStations;
	private int mSize;
	private World mWorld;
	
	private StringFlag mMinigameId;
	private IntegerFlag mSubjectDrawTime;
	private BooleanFlag mAllowSubjectDraw;
	private IntegerFlag mWaitTime;
	private BooleanFlag mSaveSubjects;
	private EnumFlag<GameMode> mMode;
	private IntegerFlag mMaxRoundTime;
	
	private HashMap<String, Flag<?>> mFlags;
	
	// In game vars
	
	private int mNextToDraw;
	private Subject mSubject;
	
	private MinigamePlayer mSubjectDrawer;
	private StateEngine<GameBoard> mEngine = new StateEngine<GameBoard>();
	
	private GameBoard()
	{
		mFlags = new HashMap<String, Flag<?>>();
		
		mMinigameId = new StringFlag();
		mFlags.put("minigame", mMinigameId);
		
		mSubjectDrawTime = new IntegerFlag();
		mSubjectDrawTime.setValue(60);
		mFlags.put("pattern-draw-time", mSubjectDrawTime);
		
		mAllowSubjectDraw = new BooleanFlag();
		mAllowSubjectDraw.setValue(true);
		mFlags.put("pattern-draw-enabled", mAllowSubjectDraw);
		
		mWaitTime = new IntegerFlag();
		mWaitTime.setValue(10);
		mFlags.put("wait-time", mWaitTime);
		
		mSaveSubjects = new BooleanFlag();
		mSaveSubjects.setValue(true);
		mFlags.put("pattern-save", mSaveSubjects);
		
		mMode = new EnumFlag<GameMode>(GameMode.class);
		mMode.setValue(GameMode.Elimination);
		mFlags.put("mode", mMode);
		
		mMaxRoundTime = new IntegerFlag();
		mMaxRoundTime.setValue(120);
		mFlags.put("max-round-time", mMaxRoundTime);
	}
	
	public GameBoard(int players, int size, String minigame, World world)
	{
		this();
		
		mStations = new PlayerStation[players];
		
		for(int i = 0; i < players; ++i)
			mStations[i] = new PlayerStation(this);
		
		mSize = size;
		mMinigameId.setValue(minigame);
		mWorld = world;
	}
	
	public GameBoard(File file, World world) throws IOException, InvalidConfigurationException
	{
		this();
		
		mWorld = world;
		read(file);
	}
	
	public World getWorld()
	{
		return mWorld;
	}
	
	public int getSubjectSize()
	{
		return mSize;
	}
	
	public Subject getSubject()
	{
		return mSubject;
	}
	
	public void setSubject(Subject subject)
	{
		mSubject = subject;
	}
	
	public void setDrawer(MinigamePlayer player)
	{
		mSubjectDrawer = player;
	}
	
	public MinigamePlayer getDrawer()
	{
		return mSubjectDrawer;
	}
	
	public MinigamePlayer selectNextDrawer()
	{
		Minigame minigame = getMinigame();
		mSubjectDrawer = minigame.getPlayers().get(mNextToDraw);
		++mNextToDraw;
		if(mNextToDraw >= minigame.getPlayers().size())
			mNextToDraw = 0;
		
		return mSubjectDrawer;
	}
	
	public long getSubjectDrawTime()
	{
		return mSubjectDrawTime.getValue() * 1000;
	}
	
	public long getWaitTime()
	{
		return mWaitTime.getValue() * 1000;
	}
	
	public long getMaxRoundTime()
	{
		return mMaxRoundTime.getValue() * 1000;
	}
	
	public boolean getSaveSubjects()
	{
		return mSaveSubjects.getValue();
	}
	
	public boolean getAllowSubjectDrawing()
	{
		return mAllowSubjectDraw.getValue();
	}
	
	public GameMode getGameMode()
	{
		return mMode.getValue();
	}
	
	public boolean isValid()
	{
		return getErrors().isEmpty();
	}
	
	public List<String> getErrors()
	{
		ArrayList<String> errors = new ArrayList<String>();
		if(getMinigame() == null)
			errors.add("Minigame " + mMinigameId + " does not exist");
		
		for(int i = 0; i < mStations.length; ++i)
		{
			if(!mStations[i].isValid())
				errors.add("Player station " + (i+1) + " is not set");
		}
		
		return errors;
	}
	
	public String getMinigameId()
	{
		return mMinigameId.getValue();
	}
	
	public Minigame getMinigame()
	{
		return Minigames.plugin.mdata.getMinigame(mMinigameId.getValue());
	}
	
	public int getStationCount()
	{
		return mStations.length;
	}
	
	public PlayerStation getStation(int number)
	{
		return mStations[number];
	}
	
	public PlayerStation getStation(MinigamePlayer player)
	{
		for(PlayerStation station : mStations)
		{
			if(station.getPlayer() == player)
				return station;
		}
		
		return null;
	}
	
	public PlayerStation[] getStations()
	{
		return mStations;
	}
	
	@Override
	public void addFlag( String name, Flag<?> flag ) throws IllegalArgumentException
	{
		if(mFlags.containsKey(name.toLowerCase()))
			throw new IllegalArgumentException("Flag already exists");
		
		mFlags.put(name.toLowerCase(), flag);
	}
	
	@Override
	public Flag<?> getFlag( String name )
	{
		return mFlags.get(name.toLowerCase());
	}
	
	@Override
	public Map<String, Flag<?>> getFlags()
	{
		return mFlags;
	}
	
	@Override
	public boolean hasFlag( String name )
	{
		return mFlags.containsKey(name.toLowerCase());
	}
	
	@Override
	public <Type> void onFlagChanged( String name, Flag<Type> flag, Type oldValue )
	{
	}
	
	public void write(File file) throws IOException
	{
		YamlConfiguration config = new YamlConfiguration();
		
		config.set("Size", mSize);
		config.set("StationCount", mStations.length);
		for(int i = 0; i < mStations.length; ++i)
		{
			ConfigurationSection section = config.createSection("Station" + i);
			mStations[i].save(section);
		}
		
		FlagIO.saveFlags(mFlags, config.createSection("flags"));
		
		config.save(file);
	}
	
	public void read(File file) throws IOException, InvalidConfigurationException
	{
		YamlConfiguration config = new YamlConfiguration();
		config.load(file);
		
		mSize = config.getInt("Size");
		int count = config.getInt("StationCount");
		mStations = new PlayerStation[count];
		for(int i = 0; i < count; ++i)
		{
			ConfigurationSection section = config.getConfigurationSection("Station" + i);
			mStations[i] = new PlayerStation(this);
			mStations[i].read(section);
		}
		
		FlagIO.loadFlags(config.getConfigurationSection("flags"), mFlags);
	}
	
	public void broadcast(String message, MinigamePlayer except)
	{
		for(MinigamePlayer player : getMinigame().getPlayers())
		{
			if(except != player)
				player.sendMessage(message, null);
		}
	}
	
	public void beginGame()
	{
		Minigame minigame = getMinigame();
		mNextToDraw = CopyCatPlugin.rand.nextInt(minigame.getPlayers().size());
		
		// Assign stations
		ArrayList<MinigamePlayer> excess = new ArrayList<MinigamePlayer>(); 
		for(int i = 0; i < minigame.getPlayers().size(); ++i)
		{
			MinigamePlayer player = minigame.getPlayers().get(i);
			
			if(i > mStations.length)
				excess.add(player);
			else
			{
				mStations[i].setPlayer(player);
				mStations[i].setCanModify(false);
				player.teleport(mStations[i].getSpawnLocation());
			}
		}
		
		// Remove excess
		for(MinigamePlayer player : excess)
		{
			player.sendMessage("There was not enough spots to place players", "error");
			Minigames.plugin.pdata.quitMinigame(player, true);
		}
		
		for(PlayerStation station : mStations)
			station.clearStation();
		
		// Begin the game
		mEngine.start(new PreRoundState(), this);
	}
	
	public void onPlayerLeave(MinigamePlayer player)
	{
		Minigame minigame = getMinigame();
		PlayerStation station = getStation(player);
		if(station != null)
		{
			station.clearStation();
			station.setPlayer(null);
			station.setCanModify(false);
		}
		
		if(minigame.getPlayers().size() <= 2)
			endGame();
		
		mEngine.sendEvent("leave", player);
	}
	
	public void onPlaceBlock(MinigamePlayer player)
	{
		mEngine.sendEvent("place", player);
	}
	
	public void endGame()
	{
		mEngine.end();
	}
	
	public State<GameBoard> getMainState()
	{
		switch(mMode.getValue())
		{
		default:
		case Elimination:
			return new EliminationMainState();
		case Score:
			return new ScoringMainState();
		}
	}
}

