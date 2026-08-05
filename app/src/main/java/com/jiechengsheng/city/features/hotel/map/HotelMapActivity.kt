package com.jiechengsheng.city.features.hotel.map


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.api.response.hotel.HotelHomeRecommend
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.EventCode
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.features.hotel.detail.HotelDetailActivity2
import com.jiechengsheng.city.features.hotel.home.HotelHomeRecommendAdapter
import com.jiechengsheng.city.features.map.HotelCustomInfoWindowAdapter
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocationUtil
import com.jiechengsheng.city.utils.PermissionUtils
import com.jiechengsheng.city.widget.calendar.JcsCalendarAdapter
import com.jiechengsheng.city.widget.calendar.JcsCalendarDialog
import org.greenrobot.eventbus.EventBus
import com.jiechengsheng.city.databinding.ActivityMapHotelBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/9/27 14:06.
 *  闂備胶澧楃敮鎺旇姳閻㈢鎹堕柡澶嬪缁?
 */
class HotelMapActivity : BaseMvpActivity<HotelMapPresenter>(), HotelMapView, JcsCalendarDialog.OnDateSelectedListener {

    private lateinit var binding: ActivityMapHotelBinding

    /** 闂備胶澧楃敮鎺旇姳閻㈢绀嗛柛鈩冪懆椤?id ,闂佹椿娼块崝宥夊春濞戙垺鍤旂€瑰嫭婢樼徊鍧楁⒑閻楀牆绗掔紒銊ョ仛缁嬪鈧綆鍓氶悾閬嶆倵濞戞瑥濮囬柛銊ユ捣閻?*/
    private var hotelCategoryId = 0

    /** 閻熸粎澧楅幐鍛婃櫠閻樿鐭楅柛灞剧懅閻熴垽鎮归崶顒佹暠闁活亙鍗抽幆鍐礋椤愩垻鈧崵绱掗銈嗘緜d */
    private var currentRequestCategoryId = 0

    /** 闂佺懓鍚嬬划搴ㄥ磼閵娾晛绀冮柛娑卞弾閸?*/
    var searchInput: String? = null

    /** 闂佸搫瀚崰鎾搭殽?*/
    var starLevel: String? = null

    /** 婵炲濞€閺€閬嶆偋閹间礁绀屾繛宸簼閿?*/
    var priceRange: String? = null

    /** 闂備胶澧楃敮鎺旇姳閻㈢绀嗛柛鈩兩戝▓?*/
    var grade: String? = null


    /** 闂佽娼欏鈥澄涢崸妤€鏋佸ù鍏兼綑濞?*/
    private var roomNumber = 1

    private lateinit var mJcsCalendarDialog: JcsCalendarDialog

    private lateinit var mStartDateBean: JcsCalendarAdapter.CalendarBean
    private lateinit var mEndDateBean: JcsCalendarAdapter.CalendarBean


    private var contentIsMap = false

    /** 闂佸憡鍔曢幊搴敊閹版澘妞?tab婵炲瓨绮岄惉鍏碱殽閸ヮ剙绀嗛柛鈩冪懆椤?*/
    private lateinit var mPagerAdapter: HotelMapPagerAdapter

    /** marker 闂備緡鍋勯ˇ顕€鎳欓幋锕€瑙﹂幖鎼灣缁€澶愭倵閻㈠灚鍤€缂併劍鐓￠幆鍐礋椤愩垻浠氶柣鐐寸◤閸斿矂宕€电硶鍋?*/
    private lateinit var mMarkerContentAdapter: HotelHomeRecommendAdapter

    /** maker  Behavior */
    private lateinit var makerBehavior: BottomSheetBehavior<RecyclerView>

    override fun isStatusDark() = true

    override fun getLayoutId() = R.layout.activity_map_hotel

    companion object {

        fun navigation(
            context: Context,
            hotelCategoryId: Int,
            starLevel: String? = null,
            priceRange: String? = null,
            grade: String? = null,
            roomNumber: Int = 1,
            startDate: JcsCalendarAdapter.CalendarBean,
            endDate: JcsCalendarAdapter.CalendarBean,
        ) {

            val bundle = Bundle().apply {
                putInt(Constant.PARAM_CATEGORY_ID, hotelCategoryId)
                putString(Constant.PARAM_STAR_LEVEL, starLevel)
                putString(Constant.PARAM_PRICE_RANGE, priceRange)
                putString(Constant.PARAM_GRADE, grade)
                putInt(Constant.PARAM_ROOM_NUMBER, roomNumber)
                putSerializable(Constant.PARAM_START_DATE, startDate)
                putSerializable(Constant.PARAM_END_DATE, endDate)
            }
            val intent = Intent(context, HotelMapActivity::class.java)
                .putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }


    override fun initView() {
        binding = ActivityMapHotelBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        initMap()
        initCategory()
        initBehavior()
        initMarkerClickListContent()
    }


    /** 婵犮垼娉涚€氼噣骞冩繝鍥х婵犲﹤鎳庨崑?*/
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras
            searchInput = bundle?.getString(Constant.PARAM_NAME, "")
            search()
        }
    }

    private fun search() {
        binding.deleteIv.visibility = if (searchInput.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.searchTv.text = searchInput
        EventBus.getDefault().post(BaseEvent<String?>(EventCode.EVENT_REFRESH_CHILD, searchInput))
        // todo marker
        requestMarkerList()
    }


    private fun initExtra() {
        val bundle = intent.extras ?: return
        bundle.apply {
            hotelCategoryId = getInt(Constant.PARAM_CATEGORY_ID)
            starLevel = getString(Constant.PARAM_STAR_LEVEL)
            priceRange = getString(Constant.PARAM_PRICE_RANGE)
            grade = getString(Constant.PARAM_GRADE)
            mStartDateBean = getSerializable(Constant.PARAM_START_DATE) as JcsCalendarAdapter.CalendarBean
            mEndDateBean = getSerializable(Constant.PARAM_END_DATE) as JcsCalendarAdapter.CalendarBean
            roomNumber = getInt(Constant.PARAM_ROOM_NUMBER, 1)
            updateDate()
        }


    }

    private fun updateDate() {
        binding.startDateTv.text = mStartDateBean.showMonthDayDateWithSplit
        binding.endDateTv.text = mEndDateBean.showMonthDayDateWithSplit

    }

    private fun initCategory() {
        mPagerAdapter = HotelMapPagerAdapter(supportFragmentManager).apply {
            starLevel = this@HotelMapActivity.starLevel
            priceRange = this@HotelMapActivity.priceRange
            grade = this@HotelMapActivity.grade
            startDateBean = mStartDateBean
            endDateBean = mEndDateBean
            roomNumberCount = this@HotelMapActivity.roomNumber
        }

    }

    private fun initBehavior() {

        makerBehavior = BottomSheetBehavior.from(binding.bottomSheetRv)
        makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        makerBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(bottomSheet: View, newState: Int) = when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    binding.contentVp.visibility = View.GONE
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    binding.contentVp.visibility = View.VISIBLE
                }
                else -> {
                }
            }
        })

    }


    private fun initMarkerClickListContent() {
        mMarkerContentAdapter = HotelHomeRecommendAdapter().apply {

            setOnItemClickListener { _, _, position ->
                val data = this.data[position]
                HotelDetailActivity2.navigation(
                    this@HotelMapActivity,
                    data.id,
                    mStartDateBean,
                    mEndDateBean,
                    starLevel,
                    priceRange,
                    grade,
                    roomNumber
                )

            }
        }


        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.bottomSheetRv)

        binding.bottomSheetRv.apply {
            adapter = mMarkerContentAdapter
            // 缂備礁鍊烽懗鍫曞极閵堝憘鐔碱敂閸曨剙鈧粯绻濇繝鍐ㄧ仴濠?
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

//                    newState == RecyclerView.SCROLL_STATE_IDLE

                }
            })
        }


    }

    override fun initData() {
        mJcsCalendarDialog = JcsCalendarDialog().apply {
            initCalendar()
            setOnDateSelectedListener(this@HotelMapActivity)
        }

        presenter = HotelMapPresenter(this)
        presenter.getHotelChildCategory(hotelCategoryId)
    }


    override fun bindListener() {
        binding.searchTv.setOnClickListener {
            searchLauncher.launch(Intent(this, SearchAllActivity::class.java).putExtra(Constant.PARAM_TYPE, 4))
            //  makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }


        binding.typeIv.setOnClickListener {


            if (contentIsMap) {
                binding.typeIv.setImageResource(R.mipmap.ic_type_map)
                makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                binding.typeIv.setImageResource(R.mipmap.ic_type_list)
                makerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            contentIsMap = !contentIsMap

        }

        binding.contentVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit


            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageSelected(position: Int) {
                if (mPagerAdapter.category.isEmpty()) return
                if (!::googleMap.isInitialized) return

                val category = mPagerAdapter.category[position]
                currentRequestCategoryId = category.id
                requestMarkerList()
                makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

        })

        binding.backIv.setOnClickListener {
            finish()
        }

        binding.deleteIv.setOnClickListener {
            searchInput = null
            search()
        }


        binding.dateLl.setOnClickListener(object :ClickUtils.OnDebouncingClickListener(500){
            override fun onDebouncingClick(v: View?) {
                mJcsCalendarDialog.show(supportFragmentManager)
            }
        })

    }

    override fun bindCategory(response: ArrayList<Category>) {
        // pager
        mPagerAdapter.category.addAll(response)
        mPagerAdapter.notifyDataSetChanged()
        binding.contentVp.offscreenPageLimit = response.size
        binding.contentVp.adapter = mPagerAdapter
        binding.tabsType.setViewPager(binding.contentVp)
    }

    // ################ 闂侀潻闄勫妯好瑰鈧幆鍕矙閸喖缍?###################

    private lateinit var googleMap: GoogleMap

    // 闂佺懓鐡ㄩ崹鐟扳枔閹寸偞濯寸€广儱娲ㄩ弸?
    private lateinit var myLocation: CameraPosition

    // 闂侀潻闄勫妯好瑰Ο鑽も枖濠电姴瀚悾閬嶆煙绾版ê浜鹃梺鍝勭墕閵嗗¨ker
    private var makers: ArrayList<Marker?> = ArrayList()


    private fun initMap() {
        // 闂佸吋鍎抽崲鑼躲亹?SupportMapFragment 濡ょ姷鍋犲▔娑橈耿椤忓牆鎹堕柡澶嬪缁傚牓鏌涢幋婵囶棞妞わ富鍓氱缓浠嬪箣閹烘垟鏋忛梺娲绘娇閸斿秴顪冮崒婊勫珰闂佸灝顑囧﹢鎾⒑椤愶絾鐨戦柣?
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.apply {

            // 缂備礁顦抽褎鎱ㄩ埡鍛闁哄鐏濆浠嬫煛閸噥娈樺?
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(Constant.LAT, Constant.LNG)))


            // 闁荤姴顑呴崯顖炲汲閿濆绀冮柛娑卞灣閺嬩繃I padding 闂傚倸鍟鍫曨敆濞岀go闁荤偞鍑归崑濠冨閸曨垰绠?
            setPadding(0, 0, 0, SizeUtils.dp2px(220f))

            // 闂佺厧顨庢禍婊堟偩閻愵剛鈻曞璺侯槷缁诲棝鏌熼褍鐏￠柣妤€宕?
            setInfoWindowAdapter(HotelCustomInfoWindowAdapter(this@HotelMapActivity))

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪
            setOnMarkerClickListener(this@HotelMapActivity)

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪婵烇絽娲犻崜婵囧閸涱垳鐜绘俊銈傚亾鐟?
            setOnInfoWindowClickListener(this@HotelMapActivity)

            // 缂備礁顦抽褎鎱ㄩ埡鍛闁绘鐗嗛悘澶愭煟閵娿儱顏х紓宥呮噽缁?
            setOnMyLocationButtonClickListener(this@HotelMapActivity)

            // 闂佺粯鍔楅幊鎾诲吹椤曗偓楠炲骞嬮悩宕囨殸婵炶揪绲界粔鍫曟偪?
            setOnMyLocationClickListener(this@HotelMapActivity)
            // 闁哄鐗嗛幊搴㈡叏椤忓牆绀夐柣鏃囶嚙閸樺瓨淇婇妞诲亾瀹曞洨顢呴梺鎸庣☉閻線銆呴锔藉剮闁哄稁鍓﹀鎺楁煕閵夈儱缍栫紒妤€鍊块幆鍐礋椤栨粌绗￠柣鐘遍檷閸婃洟宕€电硶鍋撶涵鍜佹綈鐎殿喗瀵у?
            setContentDescription("Map with lots of markers.");
        }

        googleMap.uiSettings.apply {
            isMapToolbarEnabled = true
            // 闂傚倸鎳忛崝妯何涘畝鍕闁瑰濮甸悾鍗灻归敐鍛ら柣銈呮瀹曨亞浠﹂懞銉ь洯闁?
            // isMyLocationButtonEnabled = false
        }

        // 閻熸粎澧楅幐鍛婃櫠閻樿櫕濯寸€广儱娲ㄩ弸?
        enableMyLocation()


    }

    private fun requestMarkerList() {
        if (!::googleMap.isInitialized) return
        presenter.getMakerData(search_input = searchInput, star_level = starLevel, price_range = priceRange, currentRequestCategoryId)
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::googleMap.isInitialized) return

        PermissionUtils.permissionAny(
            this, {
                if (it) {
                    googleMap.isMyLocationEnabled = true
                    getMyLocationInfo()
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // 闂佸吋鍎抽崲鑼鏉堚晙娌柡鍥╁О娴犳盯鏌涢敂鍝勫婵犫偓閹绢喖鐐婇柛娆嶅劤閻熸劙鏌ｉ妸銉ヮ仾闁哄棛鍠栭獮?
        requestMarkerList()
    }


    /**
     * 闂佸吋鍎抽崲鑼躲亹閸ヮ剙绠ｉ柟瀵稿У閻ｅ崬霉閿濆懐肖闁汇倕妫欑粚閬嶅焺閸愌呯
     */
    private fun getMyLocationInfo() {
        LocationUtil.getInstance().addressCallback = object : LocationUtil.AddressCallback {
            override fun onGetAddress(address: Address) = Unit

            override fun onGetLocation(lat: Double, lng: Double) {
                CacheUtil.getShareDefault().put(Constant.SP_MY_LATITUDE, lat.toFloat())
                CacheUtil.getShareDefault().put(Constant.SP_MY_LONGITUDE, lng.toFloat())


                // 闂傚倵鍋撴繝濠傚暔娴犲牏绱掓径搴殭濠殿喒鏅犲畷姘跺级鐠恒劎歇闂佸憡鎸哥粔宕囩礊閸涱垳纾?
                myLocation = CameraPosition.Builder().target(LatLng(lat, lng))
                    .zoom(20f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
                animateCamera(LatLng(lat, lng))
            }
        }
    }

    private fun animateCamera(location: LatLng) {
        Handler(mainLooper).postDelayed({
            val targetCamera = CameraPosition.Builder().target(location)
                .zoom(13f)
                .bearing(0f)
                .tilt(0f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(targetCamera))

        }, 10)
    }

    /**
     * 缂備礁顦抽褎鎱ㄩ埡鍛闁绘鐗嗛悘澶愭煟閵娿儱顏х紓宥呮噽缁?
     */
    override fun onMyLocationButtonClick(): Boolean {
        if (!::myLocation.isInitialized) return false
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(myLocation))
        return false
    }

    /**
     * 闂佺粯鍔楅幊鎾诲吹椤曗偓楠炲骞嬮悩宕囨殸婵炶揪绲界粔鍫曟偪?maker
     */
    override fun onMyLocationClick(location: Location) {
//        ToastUtils.showShort("Current location : \nlatitude:${location.latitude}" + "\nlongitude:${location.longitude}  ")
    }


    override fun bindMakerList(response: MutableList<HotelHomeRecommend>) {
        if (!::googleMap.isInitialized) return

        if (response.isEmpty()) {
            ToastUtils.showShort(R.string.search_result_empty_hint)
            googleMap.clear()
            makers.clear()
            return
        }

        // 闂佺儵鏅涢幉鈥斥攦閳ь剛绱掓径搴殭濠殿喒鏅犲畷姘舵儌缁烘笒er闂佽偐鍘ч崯顐⒚?
        val bounds = LatLngBounds.Builder()

        response.forEach {
            bounds.include(LatLng(it.lat, it.lng))
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))


        // 闂侀潻璐熼崝灞斤耿閹绢喖鐐婇柛娆嶅劤閻熸劖绻涢敐鍫殭濠殿喚鍋炲鍕樄闁革絿鐦瞐rker
        addMarkersToMap(response)

        // 闁诲繒鍋炲ú婊堝Φ濮濈敘rker 闂佸憡甯楅〃澶愬Υ閸愵喖鏋侀柣妤€鐗嗙粊?
        mMarkerContentAdapter.setNewInstance(response)


    }

    private fun addMarkersToMap(response: MutableList<HotelHomeRecommend>) {
        if (!::googleMap.isInitialized || response.isEmpty()) return
        googleMap.clear()
        makers.clear()

        response.forEach {

            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_hotel_maker_2, null)
            view.findViewById<AppCompatCheckedTextView>(R.id.title_tv).apply {
                text = StringUtils.getString(R.string.price_unit_format, it.price.toPlainString())
                isChecked = false
            }

            view.findViewById<ImageView>(R.id.triangle_iv).apply {
                setImageResource(R.drawable.shape_triangle_down_white)
            }

            val maker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.name)
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))
            )
            maker?.tag = it
            makers.add(maker)
        }

    }


    /**
     * 闂佺粯鍔楅幊鎾诲吹椤曗偓瀵粙宕堕宥呮暪婵炴垶鎸搁敃锕€鈻撻幋鐐碘攳闁斥晛鍟╃槐鏍磼閹邦収娈旂憸?
     */
    override fun onInfoWindowClick(marker: Marker) = Unit

    override fun onMarkerClick(marker: Marker): Boolean {
        // 闂佸湱顣介崑鎾绘煛?maker 闁荤姳绀佹晶浠嬫偪閸℃稑绠ｉ柟閭﹀幗瀵捇姊洪銏╂Х闁?
        makers.forEach {
            val makerTag = it?.tag as HotelHomeRecommend
            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_hotel_maker_2, null)
            view.findViewById<AppCompatCheckedTextView>(R.id.title_tv).apply {
                text = StringUtils.getString(R.string.price_unit_format, makerTag.price.toPlainString())
                isChecked = false
            }
            view.findViewById<ImageView>(R.id.triangle_iv).apply {
                setImageResource(R.drawable.shape_triangle_down_white)
            }

            it.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))
        }
        marker.hideInfoWindow()

        // 闁荤姳绀佹晶浠嬫偪閸℃哎浜归柟鎯у暱椤ゅ懘姊洪銏╂Х闁?
        val makerTag = marker.tag as HotelHomeRecommend
        val view = LayoutInflater.from(this).inflate(R.layout.custom_info_hotel_maker_2, null)
        view.findViewById<AppCompatCheckedTextView>(R.id.title_tv).apply {
            text = StringUtils.getString(R.string.price_unit_format, makerTag.price.toPlainString())
            isChecked = true
        }
        view.findViewById<ImageView>(R.id.triangle_iv).apply {
            setImageResource(R.drawable.shape_triangle_down_blue)
        }

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        // 闂佸憡甯掑ú锕€鐣烽崣澶嬪劅闁哄洢鍨归崝銉╂煕閹烘搩娈欓柕鍡楀暣瀵偊鎮ч崼婵堛偊
        val index = makers.indexOf(marker)
        if (index != -1) {
            binding.bottomSheetRv.scrollToPosition(index)
            makerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // 闂侀潻闄勫妯好瑰鈧悰顕€鎼归銈嗗缂備礁顦抽褎鎱ㄩ埡鍛闁规鍠楃粣妤呮煛瀹ュ懏鐭楃紓宥呮噽缁?
        Handler(mainLooper).postDelayed({

            val targetCamera = CameraPosition.Builder().target(marker.position)
                .zoom(15.5f)
                .bearing(0f)
                .tilt(0f)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(targetCamera))
        }, 10)

        return false
    }

    override fun onDateSelected(startDate: JcsCalendarAdapter.CalendarBean, endDate: JcsCalendarAdapter.CalendarBean) {
        mStartDateBean = startDate
        mEndDateBean = endDate
        updateDate()
        EventBus.getDefault().post(BaseEvent(EventCode.EVENT_REFRESH_CHILD_START_DATE, mStartDateBean))
        EventBus.getDefault().post(BaseEvent(EventCode.EVENT_REFRESH_CHILD_END_DATE, mEndDateBean))

    }


}
