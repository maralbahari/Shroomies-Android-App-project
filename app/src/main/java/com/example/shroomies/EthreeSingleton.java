package com.example.shroomies;
import android.content.Context;
import android.widget.Toast;

import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;
public class EthreeSingleton {
    private static volatile EthreeSingleton ethreeInstance  = null;
    private EThree ethree;
    private String token;
    private EthreeSingleton(Context context , String userID, String token){//
        this.token = token;
          ethree= new EThree(userID,
                  (OnGetTokenCallback) () -> getEthreeToken()
                  , context);
        Toast.makeText(context , token, Toast.LENGTH_LONG).show();

    }

    public static EthreeSingleton getInstance(Context context , String userID, String token){
        if(ethreeInstance==null){
            synchronized (EthreeSingleton.class){
                if(ethreeInstance==null){
                    ethreeInstance = new EthreeSingleton(context, userID, token);
                }
            }
        }
        return ethreeInstance;
    }
    private String getEthreeToken(){
        return token;
    }

    public EThree getEthreeInstance(){
        return ethree;
    }

    public void clearInstance(){
        ethree = null;
        ethreeInstance =null;
    }

}
