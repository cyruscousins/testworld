/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.audio.plugins.OGGLoader;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

public class MusicManager implements Runnable{
    
	public static final String splitStr = "\\s";
	
	public int songCount, songMax;
	public int characteristicCount;
	int[][] characteristics;
	
	public String[] characteristicNames;
	String[] songFileNames;
	
        AudioKey[] audioKeys;
	String[] songNames;
	String[] songComposers;

        
        //TODO what can we do with a listener?
        Listener listener = new Listener();
        AudioRenderer ar;
        AssetLoader oggLoader = new OGGLoader();
        
        AudioData musicData;
        
	public HashMap<String, Integer> characteristicIndicesMap;
	
	public MusicManager(int songCount, int characteristicCount){
		this.songMax = songCount;
		this.characteristicCount = characteristicCount;
		characteristics = new int[songCount][characteristicCount];
		characteristicNames = new String[characteristicCount];
		tempLikelihoods = new int[songCount];
	}
	
	String[] songBankFiles;
	public MusicManager(String infoFile, String[] songBankFiles, AppSettings settings){
            this.songBankFiles = songBankFiles;

            try {
                readDatabaseInfoFile(infoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            new Thread(this, "Music Bank Load Thread").start();

            ar = JmeSystem.newAudioRenderer(settings);
            ar.initialize();
            ar.setListener(listener);
            AudioContext.setAudioRenderer(ar);
        }
	
	//characteristics vector function
	public int[] getUnreducedCharacteristicVector(String[] characteristics, int[] weights){
		int[] vector = new int[characteristicCount];
		for(int i = 0; i < characteristics.length; i++){
			vector[getCharacteristicIndex(characteristics[i])] = weights[i];
		}
		return vector;
	}
	
	/*
	 * External Song playing handles
	 */

	public int getCharacteristicIndex(String s){
            Integer i = characteristicIndicesMap.get(s);
            if(i == null) return -1;
            return i.intValue();
	}
	
	int[] tempLikelihoods;
	public static Random rand = new Random();
	public int getRandomSong(int[] parameters){
            int likelihood = 0;
            for(int i = 0; i < songCount; i++){
                tempLikelihoods[i] = likelihood;
                for(int j = 0; j < characteristicCount; j++){
                    likelihood += parameters[j] * characteristics[i][j];
                }
                if(likelihood < 0) likelihood = 0; //negative means 0 chance			
            }
            int selection = rand.nextInt(likelihood);
            for(int i = 0; i < songCount; i++){
                if(selection <= tempLikelihoods[i]){
                    return i;
                }
            }
            return -1;
	}
	
	int currentSongIndex;
	AudioNode currentSong;
        
	public void playSong(int index){
		if(currentSong != null){
			stopCurrentSong();
		}
		currentSongIndex = index;
                System.out.println("Picked " + index + " of " + songCount + " max " + songMax + ".  " + audioKeys.length + " " + songNames.length);
                if(audioKeys[index] == null){
                    audioKeys[index] = new AudioKey(songFileNames[index], true, true);
                }
                
                final String fileName = "res/mus/ogg/" + songNames[index] + ".ogg";
                
                AudioKey key = new AudioKey(fileName, true, true);
                try{
                    musicData = (AudioData) oggLoader.load(new AssetInfo(null, key) {
                        @Override
                        public InputStream openStream() {
                            try{
                                return new FileInputStream(fileName);
                            }catch (FileNotFoundException ex){
                                ex.printStackTrace();
                            }
                            return null;
                        }
                    });
                }
                catch (IOException ex){
                    ex.printStackTrace();
                    currentSongIndex = -1;
                    return;
                }
                
                currentSong = new AudioNode(musicData, key);
                        
		ar.playSource(currentSong);
		System.out.println("/////Playing new song: " + index + ": " + songFileNames[index]);
	}
	
	protected void songFinished(){
		currentSong = null;
		currentSongIndex = -1;
	}
	
	public void playRandomSong(){
		if(songCount > 0){
			playSong(rand.nextInt(songCount));
		}
		else{
			System.err.println("playRandomSong: database contains no songs.");
		}
	}
	
	public void stopCurrentSong(){
		if(currentSong != null){
                    ar.stopSource(currentSong);
			currentSong = null;
			System.out.println("/////Killing Song");
		}
		else System.out.println("NULL STOP");
	}
	
	
	//Song selection stuff
	

	int countNonzeroTerms(int[] a)
	{
	  int count = 0;
	  for(int i = 0; i < a.length; i++){
	    if(a[i] != 0) count++;
	  }
	  return count;
	}
	int[] getVectorAsReduced(int[] a, int count)
	{
	  int[] newArray = new int[count];
	  int c = 0;
	  for(int i = 0; i < a.length; i++){
	    if(a[i] > 0){
	      newArray[c * 2] = i;
	      newArray[c * 2 + 1] = a[i];
	      c++;
	    }
	  }
	  return newArray;
	}

	int getDPFromReduced(int[] a, int[] b)
	{
	  int runSum = 0;
	  int bLen = b.length / 2;
	  for(int i = 0; i < bLen; i++){
	    runSum += a[b[i * 2]] * b[i * 2 + 1];
	  }
	  return runSum > 0 ? runSum : 0;
	}

	int getDPFromUnreduced(int[] a, int[]b)
	{
	  int runSum = 0;
	  for(int i = 0; i < a.length; i++){
	    runSum += a[i] * b[i];
	  }
	  return runSum > 0 ? runSum : 0;
	}

	int pickFitSong(int[] vec){
	  int c = countNonzeroTerms(vec);
	  
	  /*
	  if(c < characteristicCount / 4) //Use reduced form
	  {
	    vec = getVectorAsReduced(vec, c);
	    int[] pSums = new int[songCount];
	    int pSum = 0;
	    for(int i = 0; i < songCount; i++){
	      pSum += getDPFromReduced(characteristics[i], vec);
	      pSums[i] = pSum;
	    }
	    int r = rand.nextInt(pSum);
	    for(int i = 0; i < pSums.length; i++){
	      if(r < pSums[i]) return i;
	    }
	      System.err.println("REDUCED ERROR!");
	      return -1;
	  }
	  else //Use full form
	  {
	  
	  */
	    int[] pSums = new int[songCount];
	    int pSum = 0;
	    for(int i = 0; i < songCount; i++){
	      pSum += getDPFromUnreduced(characteristics[i], vec);
	      pSums[i] = pSum;
	    }
	    int r = rand.nextInt(pSum);
	    for(int i = 0; i < pSums.length; i++){
	      if(r < pSums[i]) return i;
	    }
	      System.err.println("FULL ERROR!");
	      return -1;
//	  }
	}
	
	public void playFitSong(int[] vec){
		playSong(pickFitSong(vec));
	}
	
	public void musicUpdate(int[] vec){
//		if(currentSong != null) System.out.println("CURDP: " +  getDPFromUnreduced(characteristics[currentSongIndex], vec)); 
		if(currentSong == null || getDPFromUnreduced(characteristics[currentSongIndex], vec) <= 0){
			playFitSong(vec);
		}
		
	}
	
	//Threaded song database read
	public void run(){
		try {
			for(int i = 0; i < songBankFiles.length; i++){
				readDatabaseSongsFile(songBankFiles[i]);
			}
		} catch (IOException e) {
			System.err.println("Music Database Load Failure");
			e.printStackTrace();
		}
	}

	//Contains the info needed to initialize a database (No actual song data).  This is necessary to move on with
	//the global initialization, reading in actual song data banks can occur in a background thread.
	void readDatabaseInfoFile(String filePath) throws IOException{

            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String[] toks = in.readLine().split(splitStr);

            characteristicCount = Integer.parseInt(toks[0]);
            songMax = Integer.parseInt(toks[1]);

            //initialize member data variables
            characteristics = new int[songMax][characteristicCount];
            characteristicNames = new String[characteristicCount];
            characteristicIndicesMap = new HashMap<String, Integer>(characteristicCount * 2);

            tempLikelihoods = new int[songMax];
            audioKeys = new AudioKey[songMax];

            songNames = new String[songMax];
            songFileNames = new String[songMax];
		
            toks = in.readLine().split(splitStr);
            for(int i = 0; i < characteristicCount; i++){
                characteristicNames[i] = toks[i].intern();
                //System.out.println("Putting " + characteristicNames[i]);
                characteristicIndicesMap.put(characteristicNames[i], i);
            }
	}

	void readDatabaseSongsFile(String filePath) throws IOException{
		System.out.println("Reading database at " + filePath);
		BufferedReader in = new BufferedReader(new FileReader(filePath));
	  
		String[] toks = in.readLine().split(splitStr);
	  
	  int localCC = Integer.parseInt(toks[0]);  //characteristics in this file
	  int localSC = Integer.parseInt(toks[1]);  //songs in this file

	  int[] localToGlobalCharacteristicMap = new int[localCC];
	  toks = in.readLine().split(splitStr);
	  for(int i = 0; i < localCC; i++){
		//System.out.println("Splitting " + toks[i]);
	    localToGlobalCharacteristicMap[i] = getCharacteristicIndex(toks[i]);
	    if(localToGlobalCharacteristicMap[i] == -1){
	      System.err.println("Error: tok " + i + " \"" + toks[i] + "\" not recognized as a valid characteristic.");
	      System.exit(1);
	    }
	  }

	  //...

	  int nextIndex = songCount;
	  for(int i = 0; i < localSC; i++){
	    toks = in.readLine().split(splitStr);
	    //System.out.println("READ " + toks[0]);
	    songNames[nextIndex] = toks[0].intern();
	    songFileNames[nextIndex] = songNames[nextIndex] + ".ogg";
	    for(int j = 0; j < localCC; j++){
	      characteristics[nextIndex][localToGlobalCharacteristicMap[j]] = Integer.parseInt(toks[j + 1]);
	    }
	    
	    nextIndex++;
	  }
	  songCount = nextIndex; //this is so we can have some degree of thread safety (multiple loads are not OK, but load and concurrent access are probably ok).
	}
	
	//Currently does nothing, as audio is instead streamed.
	public void loadSong(int i, String string){
		songFileNames[i] = string + ".ogg";
		songNames[i] = string;
		songComposers[i] = "Unknown";
		
	}
	
	
	
	
	
//	//Music creation stuff
	
	
	

	//Initialization file contains:
	//Num characteristics,
	//Maximum possible elements.
	//After reading this, a second thread to read the main file with element data is created.
	
	public static void main(String[] args)
	{
		try {
			new MusicManager(0, 0).fileGen("res/mus/bankinfo.csv", "res/mus/ogg", "res/mus/bnk/bank1.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void fileGen(String initFile, String songDirPath, String outpath) throws IOException{
	  readDatabaseInfoFile(initFile);

	  File[] dirContents = new File(songDirPath).listFiles();
	  songMax = dirContents.length;
	  songNames = new String[songMax];
	  
	  for(int i = 0; i < songMax; i++){
		  //songNames[i] = dirContents[i].getName().replaceAll("\\.[a-zA-Z0-9]*", "");
		  songNames[i] = dirContents[i].getName().replaceAll("\\.ogg", "");
	  }

	  String[] tokensToSearch = { }; //read these in from a file
	  String[] charNamesToAugment = { }; //these too

	  
	  tokensToSearch = (
			  "Stravinsky Symphony Waltz Minor " + 
			  "Debussy Piano Guitar Orchestra " +
			  "Concerto Rachmaninov Sea Ocean " +
			  "Mer Nocturne Night Arabesque " +
			  "FromTheNewWorld Chanson Funeral Firebird " +
			  "Fawn Cello String MoonlightSonata " +
			  "Sonata Allegro Andante Vivace " +
			  "FurElise").split(splitStr);
	  
	  charNamesToAugment = (
			  "Bitonal|Orchestral|Warlike Orchestral Classy Minor " + 
			  "Enchanting Piano Guitar Orchestral " + 
			  "Orchestral Piano Ocean Ocean " + 
			  "Ocean Nocturne Nocturne Arabesque " + 
			  "Orchestral|String|Wondrous Operatic Funeral Orchestral|Wind " +
			  "Orchestral|String|Wind String String Nocturne|Piano " + 
			  "Sonata Midtempo|Downtempo Midtempo|Uptempo Uptempo " +
			  "Piano").split(splitStr);
	  
	  System.out.println(
			  countChar('|', "Bitonal|Orchestral|Warlike Orchestral Classy Minor " + 
			  "Enchanting Piano Guitar Orchestral " + 
			  "Orchestral Piano Oceanic Oceanic " + 
			  "Oceanic Nocturne Nocturne Arabesque " + 
			  "Orchestral|String|Wondrous Operatic Funeral Orchestral|Wind " +
			  "Orchestral|String|Wind String String Nocturne|Piano " + 
			  "Sonata Midtempo|Downtempo Midtempo|Uptempo Uptempo " +
			  "Piano") + " | found, " + charNamesToAugment.length + " words found.");
	  int[] charAmtsToAugment = new int[]{
			  4,4,6, 6,     6,   8,     
			  8,     4,     4,   4, 
			  3,     2,     4,   4,     
			  4,     3,     3,   4,
			  8,6,4, 4,     4,   4,6,
			  6,5,5, 6,     8,   4,8,
			  8,     6,2,   2,6, 8,
			  8
			  
	  };
		
	  
	  boolean[] usedCharacteristic = new boolean[characteristicCount];
	  int usedCharacteristicCount = 0; 
	  for(int i = 0; i < tokensToSearch.length; i++){
		  tokensToSearch[i] = tokensToSearch[i].toLowerCase();
	  }

	  for(int i = 0; i < songMax; i++){
		String name = songNames[i].toLowerCase();
		int amtsIndex = 0;
	    for(int j = 0; j < tokensToSearch.length; j++){
	    	if(name.contains(tokensToSearch[j])){
		    	String[] subtoks = charNamesToAugment[j].split("\\|");
		    	System.out.println("Found " + tokensToSearch[j] + " in " + songNames[i]);
		    	System.out.println(charNamesToAugment[j] + " split to ");
		    	for(int k = 0; k < subtoks.length; k++){
		    		System.out.println(subtoks[k]);
			    	int characteristicIndex = getCharacteristicIndex(subtoks[k]);
			    	if(characteristicIndex == -1){
			    		System.err.println("ERROR: " + subtoks[k] + " not recognized.");
			    	}
			        characteristics[i][characteristicIndex] += charAmtsToAugment[amtsIndex];
			        if(!usedCharacteristic[characteristicIndex]){
			        	usedCharacteristic[characteristicIndex] = true;
			        	usedCharacteristicCount++;
			        }
			    	amtsIndex++;
			      }
	    	}
		    else amtsIndex += 1 + countChar('|', charNamesToAugment[j]);
	    }
		if(charAmtsToAugment.length != amtsIndex){
			System.err.println("ERROR: Amts count is wrong (" + amtsIndex + " / " + charAmtsToAugment.length + ").");
			System.exit(0);
		}
		System.out.println("OK");
	  }

	  //output
	  OutputStream out = new BufferedOutputStream(new FileOutputStream(outpath));

	  boolean printUnused = true; //Print even characteristics not use by any song
	  
	  out.write(((printUnused ? characteristicCount : usedCharacteristicCount) + " " + songNames.length + "\n").getBytes());
	  
	  //Print characteristics
	  boolean first = true;
	  for(int i = 0; i < characteristicCount; i++){
	    if(printUnused || usedCharacteristic[i]){
	      if(first){
	        out.write((characteristicNames[i]).getBytes());
	        first = false;
	      }
	      else{
	        out.write((" " + characteristicNames[i]).getBytes());
	      }
	    }
	  }

	  for(int i = 0; i < songMax; i++){
		//System.out.println("Outputting " + songNames[i]);
	    out.write(("\n" + songNames[i]).getBytes());
	    for(int j = 0; j < characteristicCount; j++){
	      if(printUnused || usedCharacteristic[j]){
	        out.write((" " + characteristics[i][j]).getBytes());
	      }
	    }
	  }

	  out.close();
	}
	
	static int countChar(char c, String s){
		int count = 0;
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) == c) count++;
		}
		return count;
	}
}
