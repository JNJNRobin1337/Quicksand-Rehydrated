package net.mokai.quicksandrehydrated.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.entity.sinkmodules.SinkData;

import javax.annotation.Nullable;
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



    List<SinkData> sinkData = new ArrayList<SinkData>();
    List<SinkData> getSinkData();
    void setSinkData(List<SinkData> set);
    void addSinkModule(SinkData addModule);
    boolean hasSinkModule(Class<?> cls);

    SinkData accessSinkModule(Class<?> cls);




    Vec3 tugPosition = new Vec3(0.0, 0.0, 0.0);

    Vec3 getTugPosition();
    void setTugPosition(Vec3 set);


    Vec3 tugMomentum = new Vec3(0.0, 0.0, 0.0);

    Vec3 getTugMomentum();
    void setTugMomentum(Vec3 set);


    boolean inQuicksand = false;
    boolean getInQuicksand();
    void setInQuicksand(boolean set);



    boolean quicksandEnterFlag = false;
    boolean getquicksandEnterFlag();
    void setquicksandEnterFlag(boolean set);



    BlockPos getStuckBlock(Entity pEntity);

}
