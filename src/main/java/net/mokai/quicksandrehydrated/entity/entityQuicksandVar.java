package net.mokai.quicksandrehydrated.entity;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.entity.SinkModules.SinkDataInterface;

import java.util.ArrayList;
import java.util.List;

public interface entityQuicksandVar {


    Vec3 quicksandMultiplier = Vec3.ZERO;
    Vec3 getQuicksandMultiplier();
    void setQuicksandMultiplier(Vec3 set);
    void multiOrSetQuicksandMultiplier(Vec3 set);



    Vec3 quicksandAdditive = Vec3.ZERO;
    Vec3 getQuicksandAdditive();
    void setQuicksandAdditive(Vec3 set);
    void addQuicksandAdditive(Vec3 set);
    void multiplyQuicksandAdditive(Vec3 set);



    List<SinkDataInterface> sinkData = new ArrayList<SinkDataInterface>();
    List<SinkDataInterface> getSinkData();
    void setSinkData(List<SinkDataInterface> set);
    void addSinkModule(SinkDataInterface addModule);
    boolean hasSinkModule(Class<?> cls);




    Vec3 previousPosition = new Vec3(0.0, 0.0, 0.0);

    Vec3 getPreviousPosition();
    void setPreviousPosition(Vec3 set);



    boolean inQuicksand = false;
    boolean getInQuicksand();
    void setInQuicksand(boolean set);



    boolean quicksandEnterFlag = false;
    boolean getquicksandEnterFlag();
    void setquicksandEnterFlag(boolean set);



    BlockPos getStuckBlock(Entity pEntity);

}
