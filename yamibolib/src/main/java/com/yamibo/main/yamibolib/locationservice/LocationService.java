package com.yamibo.main.yamibolib.locationservice;


import com.yamibo.main.yamibolib.locationservice.model.City;
import com.yamibo.main.yamibolib.locationservice.model.Location;
import com.yamibo.main.yamibolib.model.GPSCoordinate;

/**
 * 定位服务
 * <p/>
 * 由于中国政府的2B需求，所有国内使用的地图采用的坐标都需要纠偏，我们简称为“纠偏坐标”<br>
 * 但是定位服务获取的原始坐标是未经过纠偏的，我们简称“真实坐标”<br>
 * 真实坐标和纠偏坐标属于两套坐标系，请求Server所使用的一般为真实坐标，Server返回的一般为纠偏坐标
 *
 * @author Yimin
 */
public interface LocationService {

    /**
     * 表示当前状态为定位失败
     * <p/>
     * 可能是由于没有打开定位开关或当前信号无法支持定位
     */
    public static final int STATUS_FAIL = -1;
    /**
     * 表示当前状态为空闲
     * <p/>
     * 一般不会出现这种状态
     */
    public static final int STATUS_IDLE = 0;
    /**
     * 表示当前定位服务正在尝试获取最新的位置
     * <p/>
     * 这种状态下，可能定位服务还没有可用的位置，也可能是由于正在获取更新的位置
     */
    public static final int STATUS_TRYING = 1;
    /**
     * 表示当前定位已经完成并可以持续获取可用的位置
     */
    public static final int STATUS_LOCATED = 2;

    /**
     * 获取当前定位服务的状态
     */
    int status();

    /**
     * 当前是否有可用的位置
     */
    boolean hasLocation();

    /**
     * 获取全部的位置信息
     *
     * @return 当hasLocation()返回true时，一定不为空
     */
    Location location();

    /**
     * 真实坐标
     * <p/>
     * 即GPS芯片返回的原始坐标，未经过纠偏<br>
     * 请求Server使用的坐标一般为真实坐标
     *
     * @return 当hasLocation()返回true时，一定不为空
     */
    GPSCoordinate realCoordinate();

    /**
     * 纠偏坐标
     * <p/>
     * 经过偏移后的坐标，一般用于计算距离
     *
     * @return 当hasLocation()返回true时，一定不为空
     */
    GPSCoordinate offsetCoordinate();

    /**
     * 当前位置所在的Reverse Geocoding (RGC)地址
     *
     * @return 当hasLocation()返回true时，一定不为空
     */
    String address();

    /**
     * 当前位置所在的城市
     *
     * @return 当hasLocation()返回true时，一定不为空
     */
    City city();

    /**
     * 启动定位
     * <p/>
     * 当定位状态为失败或空闲时调用<br>
     * 如果当前系统定位开关未打开，会直接返回false
     * <p/>
     * eg.<br>
     * <code>
     * if(locationService.status() < 0) {<br>
     * &nbsp;&nbsp;if(!locationService.start()) {<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Toast.makeText(...).show();<br>
     * &nbsp;&nbsp;}<br>
     * }
     * </code>
     */
    boolean start();

    /**
     * 停止定位
     */
    void stop();

    /**
     * 刷新当前位置<br>
     * 如果当前系统定位开关未打开，会直接返回false
     */
    boolean refresh();

    void addListener(LocationListener listener);

    void removeListener(LocationListener listener);

    /**
     * 用户坐标选择回传
     * <p/>
     * LocationService可以尝试startActivity(action=
     * "com.yamibo.action.SELECT_COORDINATE",
     * "coordinates":List&lt;GPSCoordinate&gt;)<br>
     * 如果存在该Activity，则Activity结束时需回传selectCoordinate(type,selected)<br>
     * <p/>
     * 新增用途，当定位失败时，用户可以指定一个静态坐标（type=0xFF01）。
     *
     * @param type  type=0表示取消，type=1表示已选择，type=-1表示都不正确，type=0xFF01表示用户指定<br>
     * @param coord 第一个坐标为默认坐标，可以不指定coord
     */
    void selectCoordinate(int type, GPSCoordinate coord);
}
