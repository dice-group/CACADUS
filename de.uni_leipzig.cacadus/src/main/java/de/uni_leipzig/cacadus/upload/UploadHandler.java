package de.uni_leipzig.cacadus.upload;

import java.io.File;

import org.bio_gene.wookie.connection.Connection;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;
import de.uni_leipzig.cacadus.utils.Handler;

public class UploadHandler extends Thread implements Handler{
	
	private Connection con;
	
	public static final int GAME=0;
	public static final int PLAYER=1;
	public static final int EVENT=2;
	
	private String uploadFolder;
	private String graphURI;
	private int mode;
	private boolean endSignal=false;

	public UploadHandler(){
		graphURI = Config.getGraphURI();
		uploadFolder = Config.getUploadFolder();
		File f = new File(uploadFolder);
		f.mkdirs();
		f = new File(uploadFolder+File.separator+GAME);
		f.mkdir();
		f = new File(uploadFolder+File.separator+EVENT);
		f.mkdir();
		f = new File(uploadFolder+File.separator+PLAYER);
		f.mkdir();
		mode=-1;
		@SuppressWarnings("unused")
		Connection con = Config.getConnection();
	}
	
	public UploadHandler(int mode){
		graphURI = Config.getGraphURI();
		uploadFolder = Config.getUploadFolder();
		File f = new File(uploadFolder);
		f.mkdirs();
		f = new File(uploadFolder+File.separator+mode);
		f.mkdir();
		this.mode = mode;
		@SuppressWarnings("unused")
		Connection con = Config.getConnection();
	}
	
	public boolean isEmpty(){
		return new File(uploadFolder+File.separator+mode).listFiles().length==0;
	}
	
	private File[] listFiles(int mode){
		return new File(uploadFolder+File.separator+mode).listFiles();
	}
	
	public boolean isEmpty(int folder){
		switch(folder){
		case UploadHandler.GAME:
			return new File(uploadFolder+File.separator+GAME).listFiles().length==0;
		case UploadHandler.EVENT:
			return new File(uploadFolder+File.separator+EVENT).listFiles().length==0;
		case UploadHandler.PLAYER:
			return new File(uploadFolder+File.separator+PLAYER).listFiles().length==0;
		}
		return false;
	}
	
	public static boolean isEmpty(String dir, int folder){
		switch(folder){
		case UploadHandler.GAME:
			return new File(dir+File.separator+GAME).listFiles().length==0;
		case UploadHandler.EVENT:
			return new File(dir+File.separator+EVENT).listFiles().length==0;
		case UploadHandler.PLAYER:
			return new File(dir+File.separator+PLAYER).listFiles().length==0;
		}
		return false;
	}
	
	public void uploadFile(File file){
		con.uploadFile(file, graphURI);
	}
	
	private void folderLookup(){
		if(mode>=0){
			if(!FileHandler.isFolderEmpty(uploadFolder+File.separator+mode)){
				for(File f : new File(uploadFolder+File.separator+mode).listFiles()){
					if(f.getAbsolutePath().endsWith(".tmp")){
						continue;
					}
					uploadFile(f);
					f.delete();
				}
			}
		}
		else{
			for(File f : listFiles(GAME)){		
				if(f.getAbsolutePath().endsWith(".tmp")){
					continue;
				}
				uploadFile(f);
				f.delete();
			}
			for(File f : listFiles(EVENT)){				
				if(f.getAbsolutePath().endsWith(".tmp")){
					continue;
				}
				uploadFile(f);
				f.delete();
			}
			for(File f : listFiles(PLAYER)){				
				if(f.getAbsolutePath().endsWith(".tmp")){
					continue;
				}
				uploadFile(f);
				f.delete();
			}
		}
	}
	
	//Should be sended by the process before the UploadHandler (e.g. the LimesHandler)
	public void sendEndSignal(){
		this.endSignal=true;
	}
	
	public void startLookup(){
		//Look for folder with files ready to upload
		while(!endSignal){
			folderLookup();
		}
		folderLookup();
	}
	
	public void run(){
		startLookup();
	}
	
}
