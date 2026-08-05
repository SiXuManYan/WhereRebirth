package com.jiechengsheng.city.features.gourmet.restaurant.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.area.AreaResponse
import com.jiechengsheng.city.api.response.category.Category
import com.jiechengsheng.city.api.response.gourmet.restaurant.RestaurantResponse
import com.jiechengsheng.city.base.BaseEvent
import com.jiechengsheng.city.base.mvp.BaseMvpActivity
import com.jiechengsheng.city.bean.RestaurantListRequest
import com.jiechengsheng.city.features.gourmet.cart.ShoppingCartActivity
import com.jiechengsheng.city.features.gourmet.restaurant.detail.RestaurantDetailActivity
import com.jiechengsheng.city.features.gourmet.restaurant.list.filter.more.MoreFilterFragment.MoreFilter
import com.jiechengsheng.city.features.gourmet.takeaway.TakeawayActivity
import com.jiechengsheng.city.features.map.HotelCustomInfoWindowAdapter
import com.jiechengsheng.city.features.search.SearchAllActivity
import com.jiechengsheng.city.utils.CacheUtil
import com.jiechengsheng.city.utils.Constant
import com.jiechengsheng.city.utils.LocationUtil
import com.jiechengsheng.city.utils.PermissionUtils
import com.jiechengsheng.city.view.empty.EmptyView
import com.jiechengsheng.city.widget.list.DividerDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.jiechengsheng.city.databinding.ActivityGourmetListBinding
import android.view.ViewGroup

/**
 * Created by Wangsw  2021/3/24 13:56.
 * 缂傚倸鍊搁崐鎼佸磹閹间礁纾圭紒瀣紩濞差亜围濠㈣泛顑囬崢鎰版⒑閹稿海绠撻柨娑樻瀵板嫰骞囬鈹洦鐓曢柍钘夋楠炴鐥ィ鍐暫婵﹥妞藉畷顐﹀礋椤撴稒鐎遍梻浣告啞閿氱紓宥咃躬瀹曟椽鍩€?
 */
class RestaurantHomeActivity : BaseMvpActivity<RestaurantHomePresenter>(), RestaurantHomeView {

    private lateinit var binding: ActivityGourmetListBinding


    private var mCategoryId = 0
    private var mPidName = ""

    private var page = Constant.DEFAULT_FIRST_PAGE

    private lateinit var emptyView: EmptyView
    private lateinit var mRequest: RestaurantListRequest

    /** 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒佹濠德板€曢幊宀勫焵椤掆偓閸燁垰顕ラ崟顖氱疀妞?*/
    private lateinit var mAdapter: DelicacyAdapter

    /** marker 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涢弴銊ョ€柛銉墻閺佸鏌嶈閸撶喖鐛繝鍌楁瀻闁规儳纾娲⒑閹稿孩纾甸柛瀣崌閹嘲鈻庡▎鎴犳殼濡ょ姷鍋為悧婊呮閹烘鍤嬫い鎺嗗亾闁逞屽墯濞叉﹢寮查懜鐐瑰亝闁告劏鏂侀幏娲⒑閸涘﹦鈽夐柨鏇樺€濋幃妯绘綇閵娧咁啎闂佸憡绮岄崯浼村礆娴煎瓨鐓?item */
    private lateinit var mMarkerContentAdapter: DelicacyAdapter


    /** maker  Behavior */
    private lateinit var makerBehavior: BottomSheetBehavior<RecyclerView>

    /** 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭罕闂佸搫娲㈤崹鍦不鐟欏嫨浜滈柟鏉垮閻ｉ亶鏌涢妸銉モ偓鍧楃嵁閺嶎灔搴敆閳ь剚淇婃總鍛婄厵闁荤喐澹嗛崺锝夋煛鐏炵澧茬€垫澘瀚埀顒婄秵娴滃爼宕电€ｎ剛纾藉ù锝囶焾閳ь剚鎮傞幃褔骞樼拠鑼舵憰濠电偞鍨堕敃鈺呮偂閵夆晜鐓曢柟浼存涧楠炴牗銇勮箛锝呭籍婵﹥妞藉畷婊堟嚑椤掆偓鐢儲绻涚€涙鐭岄柛瀣ㄥ€濋悰顕€宕橀埡渚囧殼闂佸搫顦伴崵锕傚煛閸涱厼鐝樺銈呯箰閹冲繑鍒婇幘顔界厱闁圭偓娼欑徊缁樼箾?*/
    private var contentIsMap = false

    // ################ 闂傚倸鍊搁崐椋庢濮橆剦鐒藉┑鐘崇閳锋棃鏌涢弴銊ヤ航闁绘柨妫欐穱濠囶敍濮樸儱浠洪梺鑽ゅ枛閸嬪﹤顭囬妸鈺傜厓鐟滄粓宕滈悢鑲╁祦闁糕剝绋戠粈瀣亜閹捐泛啸闁绘挻鎹囧娲礈閼碱剙甯ラ梺鍝ュУ閻楁粎妲?###################

    private lateinit var googleMap: GoogleMap

    // 闂傚倸鍊搁崐鐑芥嚄閸洖绠犻柟鍓х帛閸嬨倝鏌曟繛鐐珔闁搞劌鍊块弻锝夋偄缁嬫妫庨梺鍝勵儐濡啴寮婚悢琛″亾閻㈡鐒惧ù鐙呯畱閳规垿顢涘鐓庢闂侀€炲苯澧紒鐘茬Ч瀹曟洟宕￠悘缁樻そ婵℃悂鍩℃担绋挎?
    private lateinit var myLocation: CameraPosition

    // 闂傚倸鍊搁崐椋庢濮橆剦鐒藉┑鐘崇閳锋棃鏌涢弴銊ヤ航闁绘柨妫欐穱濠囶敍濮樸儱浠洪梺鑽ゅ枑鐎氬牓鎮㈤崗鐓庢闂侀潧鐗嗛崐褰掓倶閸愩劉鏀介柣妯虹仛閺嗏晛鈹戦纰卞殶闁逞屽墯閸戝綊宕ｉ崘顔肩畺濡わ絽鍟悞鑲┾偓骞垮劚濡盯宕㈤悽鐢电＜闁告挆鍐炬毉闂佹寧鑹鹃湁婵犲﹤鐗忛悾鐑樻叏婵犲啯銇濈€规洦鍋婂畷鐔碱敇閻戝棙姣囬梻鍌欑窔濞佳兾涘Δ鍐Уker
    private var makers: ArrayList<Marker?> = ArrayList()


    override fun isStatusDark() = true


    override fun getLayoutId() = R.layout.activity_gourmet_list


    /** 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊勫床婵犻潧顑呴悙濠勬喐韫囨侗鏁婇柟閭﹀厴閺€浠嬫煟濡鍤嬬€规悶鍎甸弻鐔煎礃閺屻儱寮伴悗?*/
    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras
            mRequest.search_input = bundle?.getString(Constant.PARAM_NAME, "")
            search()
        }
    }


    override fun initView() {
        binding = ActivityGourmetListBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)!!)
        initExtra()
        initFilter()
        initList()
        initMap()
        initBehavior()
        initMarkerClickListContent()
    }


    private fun initExtra() {
        val bundle = intent.extras ?: return
        mCategoryId = bundle.getInt(Constant.PARAM_PID, 89)
        mPidName = bundle.getString(Constant.PARAM_PID_NAME, "")
        binding.foodTv.text = mPidName
    }

    private fun initFilter() {
        binding.filterLayout.filterPager.offscreenPageLimit = 2
        val adapter = RestaurantPagerAdapter(supportFragmentManager, 0)
        adapter.pid = mCategoryId
        adapter.pidName = mPidName
        binding.filterLayout.filterPager.adapter = adapter
    }


    private fun initList() {

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒佹濠德板€曢幊宀勫焵椤掆偓閸燁垰顕ラ崟顖氱疀妞?
        emptyView = EmptyView(this).apply {
            setEmptyImage(R.mipmap.ic_empty_search)
            setEmptyMessage(R.string.empty_search_message)
            setEmptyHint(R.string.empty_search_hint)
            addEmptyList(this)
        }

        mAdapter = DelicacyAdapter().apply {
            setEmptyView(emptyView)
            loadMoreModule.isAutoLoadMore = true
            loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            addChildClickViewIds(R.id.takeaway_support_tv)
            setOnItemChildClickListener { _, _, position ->
                onItemChildClick(this, position)
            }
            setOnItemClickListener { _, _, position ->
                onItemClick(this, position)
            }
            loadMoreModule.setOnLoadMoreListener(this@RestaurantHomeActivity)
        }
        binding.recycler.apply {
            adapter = mAdapter
            addItemDecoration(
                DividerDecoration(
                    Color.TRANSPARENT,
                    SizeUtils.dp2px(16f),
                    SizeUtils.dp2px(8f),
                    SizeUtils.dp2px(8f)
                ).apply {
                    setDrawHeaderFooter(false)
                })
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

    }


    private fun initMap() {
        // 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵?SupportMapFragment 濠电姴鐥夐弶搴撳亾濡や焦鍙忛柣鎴ｆ绾惧鏌ｅΟ鑽ゃ偞闁哄鐗楃换娑㈠箣閻戝洤鍙曢梺鑲╁鐎笛囧Φ閸曨喚鐤€闁规崘娉涢。娲⒑閸︻厽鍤€闁绘牕銈稿濠氬焺閸愩劎绐為柣搴秵娴滃爼宕电€ｎ剛纾藉ù锝囶焾閳ь剚鎮傞幃褔骞樼拠鑼舵憰濠电偞鍨崹鍦不閿濆棛绡€闂傚牊绋掔粊鈺備繆椤愩倕浠滄い顏勫暣婵℃儼绠涢幘鑸敌掗梻浣告啞閹歌顫濋妸褏纾介柟鎯ь嚟椤╃兘鎮楅敐搴濈敖妞ゆ挻妞藉娲捶椤撶偛濡洪梺鎼炲妿閸犳牕顕ｅ宀€鐤€婵炴垶鐟㈤幏鍝勨攽閻愯泛鐨虹紒顕呭灡缁绘盯宕堕浣哄幈闂佸搫鍊藉▔鏇炵摥婵＄偑鍊ゆ禍婊堝疮閺夋垹鏆﹂柟鐑樺焾閻撱儵鏌涢弴銊ヤ簽鐟滅増鎸冲濠氬磼濞嗘埈妲梺璇茬箲缁诲牓銆侀弽顓炵倞鐟滄繈鎳撻崸妤佺厵缂備降鍨归弸鐔兼煃闁垮鐏﹂柕鍥у楠炲洭宕滄担鑽锋垿姊哄Ч鍥у闁搞劌娼″?
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }

    private fun initBehavior() {

        makerBehavior = BottomSheetBehavior.from(binding.bottomSheetRv)
        makerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        makerBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(bottomSheet: View, newState: Int) = when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    binding.recycler.visibility = View.GONE
                    binding.swipeLayout.isEnabled = false
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    binding.recycler.visibility = View.VISIBLE
                    binding.swipeLayout.isEnabled = !binding.recycler.canScrollVertically(-1)
                }
                else -> {
                }
            }
        })

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                binding.swipeLayout.isEnabled = (!binding.recycler.canScrollVertically(-1) && !contentIsMap)
            }
        })
    }


    private fun initMarkerClickListContent() {
        mMarkerContentAdapter = DelicacyAdapter().apply {
            addChildClickViewIds(R.id.takeaway_ll)
            setOnItemChildClickListener { _, _, position ->
                onItemChildClick(this, position)
            }
            setOnItemClickListener { _, _, position ->
                onItemClick(this, position)
            }
        }

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.bottomSheetRv)

        binding.bottomSheetRv.apply {
            adapter = mMarkerContentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.bottomSheetRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (newState == RecyclerView.SCROLL_STATE_IDLE && makers.size > firstItemPosition) {
                    val marker = makers[firstItemPosition]
                    marker?.let {
                        onMarkerClick(it)
                    }
                }
            }
        })


    }


    override fun initData() {
        presenter = RestaurantHomePresenter(this)
        mRequest = RestaurantListRequest().apply {
            category_id = mCategoryId

            val latLng = CacheUtil.getSafeSelectLatLng()

            lat = latLng.latitude
            lng = latLng.longitude
        }
        page = Constant.DEFAULT_FIRST_PAGE
        presenter.getList(page, mRequest)
    }

    override fun bindListener() {

        binding.swipeLayout.setOnRefreshListener {

            when {
                mMarkerContentAdapter.data.isEmpty() -> {
                    presenter.getMakerData(mRequest)
                }
            }
            page = Constant.DEFAULT_FIRST_PAGE
            presenter.getList(page, mRequest)
        }

        binding.backIv.setOnClickListener { finish() }
        binding.deleteIv.setOnClickListener { onClearSearchClick() }
        binding.areaFilterLl.setOnClickListener { onAreaFilterClick() }
        binding.foodFilterLl.setOnClickListener { onFoodFilterClick() }
        binding.otherFilterLl.setOnClickListener { onOtherFilterClick() }
        binding.filterLayout.dismissView.setOnClickListener { onFilterDismissClick() }
        binding.filterLayout.filterPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageSelected(position: Int) {
                for (i in 0 until binding.categoryLl.childCount) {
                    changeFilterTabStyle(position, i)
                }
            }
        })

        binding.searchTv.setOnClickListener {
            searchLauncher.launch(Intent(this, SearchAllActivity::class.java).putExtra(Constant.PARAM_TYPE, 7))
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

        binding.cartIv.setOnClickListener {
            startActivityAfterLogin(ShoppingCartActivity::class.java)
        }
    }

    private fun search() {
        val searchInput = mRequest.search_input
        binding.deleteIv.visibility = if (searchInput.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.searchTv.text = searchInput
        onRefresh()
    }

    private fun onClearSearchClick() {
        binding.searchTv.text = ""
        if (mRequest.search_input.isNotBlank()) {
            mRequest.search_input = null
            onRefresh()
        }
    }

    fun onRefresh() {
        page = Constant.DEFAULT_FIRST_PAGE
        presenter.getList(page, mRequest)
        presenter.getMakerData(mRequest)
    }

    override fun onLoadMore() {
        page++
        presenter.getList(page, mRequest)
    }

    fun onItemChildClick(adapter: DelicacyAdapter, position: Int) {

        val data = adapter.data[position]
        val bundle = Bundle()
        bundle.putString(Constant.PARAM_ID, data.id.toString())
        startActivity(TakeawayActivity::class.java, bundle)
    }

    fun onItemClick(adapter: DelicacyAdapter, position: Int) {
        val data = adapter.data[position]
        startActivity(RestaurantDetailActivity::class.java, Bundle().apply {
            putInt(Constant.PARAM_ID, data.id)
        })
    }

    override fun bindList(data: MutableList<RestaurantResponse>, isLastPage: Boolean) {
        binding.swipeLayout.isRefreshing = false
        val loadMoreModule = mAdapter.loadMoreModule
        if (data.isEmpty()) {
            if (page == Constant.DEFAULT_FIRST_PAGE) {
                mAdapter.setNewInstance(null)
                loadMoreModule.loadMoreComplete()
                emptyView.showEmptyContainer()
            } else {
                loadMoreModule.loadMoreEnd()
            }
            return
        }
        if (page == Constant.DEFAULT_FIRST_PAGE) {
            mAdapter.setNewInstance(data)
            loadMoreModule.checkDisableLoadMoreIfNotFullPage()
        } else {
            mAdapter.addData(data)
            if (isLastPage) {
                loadMoreModule.loadMoreEnd()
            } else {
                loadMoreModule.loadMoreComplete()
            }
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭罕闂佸搫娲㈤崹鍦不鐟欏嫨浜滈柟鏉跨埣濡绢噣鏌涚€ｎ亜鈧湱鎹㈠☉銏犲耿婵☆垵娅ｆ禒鍏间繆閻愭鏀版繛鍛礈閹广垹鈽夐姀鐘殿吅闂佸搫鍟犻崑鎾绘煕閿濆嫬宓嗛柡?
     */
    private fun onAreaFilterClick() = switchFilterPager(0)

    /**
     * 缂傚倸鍊搁崐鎼佸磹閹间礁纾圭紒瀣紩濞差亜围濠㈣泛顑囬崢鎰版⒑閹稿海绠撻柨娑樻瀵板嫰骞囬鈹洦鐓曢柕澶堝灪濞呭懘鏌￠崱姗嗘疁闁哄矉缍侀幃鈺呭矗婢跺备鏁嶉梺璇茬箰缁绘垿鎮烽埡鍛槬闁逞屽墯閵囧嫰骞掗幋婵愪紝濠碘槅鍋呴崹鍦閹烘鏁嬮柍褜鍓欓…鍥樄闁糕斂鍎插鍕箛椤掑倸骞€闂備礁鐤囧銊ヮ渻閹烘挾鐝?
     */
    private fun onFoodFilterClick() = switchFilterPager(1)

    /**
     * 闂傚倸鍊搁崐鐑芥嚄閸洍鈧箓宕奸姀鈥冲簥闂佺懓鐡ㄧ换鎰版嚀閸ф鐓曢柨鏃囶嚙楠炴牠鏌涢妸锔剧畺闁靛洤瀚伴獮鎺楀箣濠靛啫浜剧紓浣骨滄禒姘舵煣韫囷絽浜炵紒鐘荤畺閺岀喓鈧稒蓱閳锋劙鏌涢敐鍕祮闁?
     */
    private fun onOtherFilterClick() = switchFilterPager(2)

    private fun onFilterDismissClick() {
        handleFilterVisible(View.GONE)

        // 濠电姷鏁告慨鐑藉极閹间礁纾婚柣鎰惈缁犱即鏌熼梻瀵割槮缂佺姷濞€閺岀喖鎮ч崼鐔哄嚒闂佺粯鎸婚敃銏ゅ蓟閳╁啫绶炴俊顖滅帛閻濇紱闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涢弴銊ョ€柛銉墻閺佸鏌嶈閸撶喖鐛繝鍌楁瀻闁规儳纾娲⒑閹稿孩顥嗘俊顐㈠缁傚秷銇愰幒鎾嫼闂佺粯鎸哥€涒晠骞夐崨濞夸簻闁挎繂瀚惌娆戔偓?
        val childTabLL = binding.categoryLl.getChildAt(binding.filterLayout.filterPager.currentItem) as LinearLayout
        val tabText = childTabLL.getChildAt(0) as CheckedTextView
        val tabImage = childTabLL.getChildAt(1) as ImageView
        tabText.isChecked = false
        tabImage.setImageResource(R.mipmap.ic_arrow_filter_black)
    }

    private fun switchFilterPager(index: Int) {
        val currentItem = binding.filterLayout.filterPager.currentItem
        if (binding.filterLayout.filterContainerLl.visibility == View.GONE) {
            handleFilterVisible(View.VISIBLE)
            // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒佹闂佸湱鍎ら崵姘洪鍛珖闂侀€炲苯澧撮柟顕€绠栭幃婊堟嚍閵夈儱澹庨梻浣藉吹婵厼鈻旈埡鍛拻闁稿本鐟ㄩ崗宀€绱掗鍛仸闁炽儻绠撳畷濂稿Ψ閵夛附袣闂備礁鎼粙渚€宕㈡總鍛婂€?
            changeFilterTabStyle(currentItem, index)
        } else {
            if (currentItem == index) {
                handleFilterVisible(View.GONE)

                // 濠电姷鏁告慨鐑藉极閹间礁纾婚柣鎰惈缁犱即鏌熼梻瀵割槮缂佺姷濞€閺岀喖鎮ч崼鐔哄嚒闂佺粯鎸婚敃銏ゅ蓟閳╁啫绶炴俊顖滅帛閻濇紱闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涢弴銊ョ€柛銉墻閺佸鏌嶈閸撶喖鐛繝鍌楁瀻闁规儳纾娲⒑閹稿孩顥嗘俊顐㈠缁傚秷銇愰幒鎾嫼闂佺粯鎸哥€涒晠骞夐崨濞夸簻闁挎繂瀚惌娆戔偓?
                val childTabLL = binding.categoryLl.getChildAt(index) as LinearLayout
                val tabText = childTabLL.getChildAt(0) as CheckedTextView
                val tabImage = childTabLL.getChildAt(1) as ImageView
                tabText.isChecked = false
                tabImage.setImageResource(R.mipmap.ic_arrow_filter_black)
            }
        }
        binding.filterLayout.filterPager.setCurrentItem(index, true)
    }

    private fun handleFilterVisible(visibility: Int) {
        binding.filterLayout.filterContainerLl.visibility = visibility
    }

    private fun changeFilterTabStyle(pagerPosition: Int, tabIndex: Int) {
        val childTabLL = binding.categoryLl.getChildAt(tabIndex) as LinearLayout
        val tabText = childTabLL.getChildAt(0) as CheckedTextView
        val tabImage = childTabLL.getChildAt(1) as ImageView
        if (pagerPosition == tabIndex) {
            tabText.isChecked = true
            tabText.paint.isFakeBoldText = true
            tabImage.setImageResource(R.mipmap.ic_arrow_filter_blue)
        } else {
            tabText.isChecked = false
            tabText.paint.isFakeBoldText = false
            tabImage.setImageResource(R.mipmap.ic_arrow_filter_black)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onEventReceived(baseEvent: BaseEvent<*>) {
        super.onEventReceived(baseEvent)
        val data = baseEvent.data
        if (data is AreaResponse) {
            // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭罕闂佸搫娲㈤崹鍦不鐟欏嫨浜滈柟鏉跨埣濡绢噣鏌涚€ｎ亜鈧湱鎹㈠☉銏犲耿婵☆垵娅ｆ禒鍏间繆閻愭鏀版繛鍛礈閹广垹鈽夐姀鐘殿吅闂佸搫鍟犻崑鎾绘煕閿濆嫬宓嗛柡?
            mRequest.trading_area_id = data.id
            val name = data.name
            if (!TextUtils.isEmpty(name)) {
                binding.areaTv.text = name
            }
        } else if (data is Category) {
            // 缂傚倸鍊搁崐鎼佸磹閹间礁纾圭紒瀣紩濞差亜围濠㈣泛顑囬崢鎰版⒑閹稿海绠撻柨娑樻瀵板嫰骞囬鈹喚鐔嗛悹铏瑰皑閸旂喐銇勯敂璇蹭簻闁宠鍨堕獮濠囨煕婵犲啫濮嶇€规洘鍨块獮妯肩磼濡厧骞堥梻浣哥枃濡椼劑鎮為敃鍋瑰鎮╅悽鐢碉紲濡炪倖娲嶉崑鎾寸箾娴ｅ啿娲ら悡鈥愁熆閼搁潧濮囬柣鎾村灴閺屽秵娼悧鍫▊婵?
            val id = data.id
            if (id == 0) {
                mRequest.category_id = null
            } else {
                mRequest.category_id = id
            }
            val name = data.name
            if (!TextUtils.isEmpty(name)) {
                binding.foodTv.text = name
            }
        } else if (data is MoreFilter) {
            // 闂傚倸鍊搁崐鐑芥嚄閸洍鈧箓宕奸姀鈥冲簥闂佺懓鐡ㄧ换鎰版嚀閸ф鐓曢柨鏃囶嚙楠炴牠鏌涢妸锔剧畺闁靛洤瀚伴獮鎺楀箣濠靛啫浜剧紓浣骨滄禒姘舵煣韫囷絽浜炵紒鐘荤畺閺岀喓鈧稒蓱閳锋劙鏌涢敐鍕祮闁?
            val more = data
            mRequest.per_price = more.per_price
            mRequest.service = more.service
            mRequest.sort = more.sort
        }
        onRefresh()
        binding.filterLayout.dismissView.performClick()
    }

    override fun bindMakerList(response: MutableList<RestaurantResponse>) {
        if (!::googleMap.isInitialized) return

        if (response.isEmpty()) {
            ToastUtils.showShort(R.string.search_result_empty_hint)
            googleMap.clear()
            makers.clear()
            return
        }

        // 闂傚倸鍊搁崐鐑芥嚄閸洖纾块柣銏㈩焾閻ょ偓绻涢幋娆忕仼缂佺姷鍠栭弻宥夊Ψ閿旂晫浼囬梺琛″亾闁挎洖鍊归悡鍐喐濠婂牆绀堟繛鎴炵椤洟鏌熼悜妯诲碍缂佸墎鍋ら獮鏍箹椤撶偞鐏堝┑鐐茬墱閸樼晫鎹㈠┑鍡忔灁闁割煈鍠楅悘鈧梻浣告惈閹虫劖鎱ㄩ幘顔藉仼鐎瑰嫰鍋婂鈺呮煠閹间焦娑ч柛鎿冨墰缁辨挻鎷呴崫鍕缂備焦顨嗘慨鈧瑀闂傚倸鍊搁崐宄懊归崶顒€纾婚柟鐗堟緲閸屻劎鎲搁弬璺ㄦ殾妞ゆ牜鍎愰弫鍐煃?
        val bounds = LatLngBounds.Builder()

        response.forEach {
            bounds.include(LatLng(it.lat, it.lng))
        }

        if (DeviceUtils.isTablet()) {
            if (response.isNotEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(response[0].lat, response[0].lng),15f))
            }
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 300))
        }


        // 闂傚倸鍊搁崐椋庢濮橆剦鐒介柤濮愬€栫€氬鏌ｉ弮鍌氬付缂佲偓婢舵劖鍊堕柣鎰摠閻忣噣鏌ら弶鎸庡仴闁哄瞼鍠撶槐鎺楀閻樺磭浜梻浣虹帛閸旀洟鏁冮鍫濊摕婵炴垶菤閺嬪海鈧箍鍎遍幊搴㈡叏閵忋倖鈷戦柤濮愬€曢弸鎴︽煕閺冣偓閻楁粓骞戦姀鐘闁靛繆鍓濆▍婊堟⒑閸撴彃浜介柛瀣閳绘捇顢橀悢铏圭槇濠电偛鐗嗛悘婵嬪几閵堝鐓曢悗锝庡墮娴犳粍銇勯弴妯哄姦鐎规洜鍠栭、姘跺川椤旇鐎搁梻鍌氬€风欢姘焽閼姐倕绶ら柛褎顨嗛崕濠囨煟椤掍椒鎴穔er
        addMarkersToMap(response)

        // 闂傚倸鍊峰ù鍥敋瑜忛幑銏ゅ箳濡も偓绾惧鏌ｉ幇顖ｆ⒖闁告繂瀚烽悡銉╂煕椤愶絾鍎曢柨鏃囧Г閸欏繑绻濋棃娑氬闁哄懐鑵慿er 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒佹濠德板€曢幊宀勫焵椤掆偓閸燁垰顕ラ崟顖氱疀妞ゆ垟鏂傞崕鐢稿蓟濞戙垹绠涢梻鍫熺⊕閻忓牓姊洪崫鍕靛剮缂佽埖宀稿濠氭晲閸涘倹妫冮崺鈧い鎺戝閸嬪鏌涢埄鍐噮闁?
        mMarkerContentAdapter.setNewInstance(response)
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐粹拻闁割偆鍠庨崰鏇㈡煠閺夋寧鍋ラ柡宀€鍠撶槐鎺楀閻樺磭浜梻浣虹帛閸旀洟鏁冮鍫濊摕婵炴垶菤閺嬪海鈧箍鍎遍幊搴㈡叏閵堝鈷戦柛娑橆煭娓氭盯鏌涢幘瀛樼殤闁瑰箍鍨归埥澶愬閳╁啯鐝抽梻浣稿閸嬫帡宕戦崟顐熸灁妞ゆ挾鍠撶弧鈧?Maker
     */
    private fun addMarkersToMap(response: MutableList<RestaurantResponse>) {
        if (!::googleMap.isInitialized || response.isEmpty()) return
        googleMap.clear()
        makers.clear()

        response.forEach {

            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            val image_iv = view.findViewById<ImageView>(R.id.image_iv)
            title_tv.text = it.title
            image_iv.setImageResource(R.mipmap.ic_marker_common_food)

            val maker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.title)
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))
            )
            maker?.tag = it
            makers.add(maker)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.apply {

            // 缂傚倸鍊搁崐鎼佸磹妞嬪海鐭嗗〒姘ｅ亾閽樻繈鏌熼崜浣烘憘闁轰礁顑囬幉鍛婃償閵娿儳鐤勯梺闈浥堥弲娑㈡偂濞戙垺鐓曟繛鎴濆船楠炴ɑ銇勯敂璇蹭喊婵﹥妞藉畷顐﹀礋閸倣锕傛⒑鐠囪尙绠茬紒璇插€块、姘舵晲閸℃瑯娴勯柣搴秵閸嬪棝宕㈤幖浣光拺闁告挻褰冩禍鐐烘煕閿濆啫鍔氶柍缁樻尭铻ｅ〒姘煎灣閸?
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(Constant.LAT, Constant.LNG)))


            // 闂傚倸鍊峰ù鍥х暦閸偅鍙忛柟缁㈠櫘閺佸嫰鏌涘☉娆愮稇闁汇値鍠楅妵鍕冀椤愵澀绮跺┑顔款潐閻擄繝寮婚妸銉㈡斀闁糕檧鏅滅瑧缂傚倷璁查崑鎾绘煕閹伴潧鏋熼柣鎾存礃缁绘盯骞嬪┑鍡氬煘闂佽楠忕紞渚€寮婚敍鍕ㄥ亾閿濆倹娅囩紒鐘虫箯 padding 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸閻ゎ喗銇勯幇鈺佺労闁搞倖娲熼弻娑㈩敃閿濆棗顦╅梺杞扮濡瑧鎹㈠☉妯兼殕闁逞屽墴钘熼柡澶婃闂傚倸鍊峰ù鍥х暦閻㈢纾婚柣鎰惈缁€鍕喐閻楀牆绗掔痪鎹愵嚙閳规垿鎮╅幓鎺撴濡炪倖娲熸禍鍫曞蓟濞戙垹鍗抽柕濞垮劤娴犫晝绱?
            setPadding(0, 0, 0, SizeUtils.dp2px(220f))

            // 闂傚倸鍊搁崐鐑芥嚄閸洖鍌ㄧ憸鏃堝Υ閸愨晜鍎熼柕蹇嬪焺濞茬鈹戦悩璇у伐闁绘锕畷鎴﹀煛閸涱喚鍘介梺閫涘嵆濞佳勬櫠娴煎瓨鐓冪紓浣股戠粈鈧銈庡弨濞夋洟骞戦崟顒傜懝妞ゆ牗鍒€閵壯呯＝濞达絿鐡旈崵娆愪繆椤愶絿绠炵€殿喖顭烽幃銏㈠枈鏉堛劍娅嶉柣鐔哥矊缁夊綊骞冨鈧弫鎾绘偐瀹曞洤骞楁繝寰锋澘鈧劙宕戦幘鍨涘亾鐟欏嫭澶勯柛鎾跺枛閵?
            setInfoWindowAdapter(HotelCustomInfoWindowAdapter(this@RestaurantHomeActivity))

            // 闂傚倸鍊搁崐鐑芥嚄閸撲礁鍨濇い鏍仜缁€澶嬩繆閵堝懏鍣圭紒鐘靛█閺岀喖骞戦幇闈涙闂佸憡淇洪～澶愬Φ閸曨垰鍗虫俊銈傚亾濞存粓绠栭幃妤€鈻撻崹顔界亞缂備緡鍠楅悷銉╂偩閻戣棄閱囬柡鍥ュ妽閺呮粓鎮楅獮鍨姎闁瑰啿绻樺?
            setOnMarkerClickListener(this@RestaurantHomeActivity)

            // 闂傚倸鍊搁崐鐑芥嚄閸撲礁鍨濇い鏍仜缁€澶嬩繆閵堝懏鍣圭紒鐘靛█閺岀喖骞戦幇闈涙闂佸憡淇洪～澶愬Φ閸曨垰鍗虫俊銈傚亾濞存粓绠栭幃妤€鈻撻崹顔界亞缂備緡鍠楅悷銉╂偩閻戣棄閱囬柡鍥ュ妽閺呮粓鎮楅獮鍨姎闁瑰啿绻樺鎶筋敂閸曘劍鏂€闂佺粯鍨靛ú銊╁礆閻楀牏绠鹃柤纰卞墮閳诲牏鈧娲樼划宀勫煘閹达箑鐐婄憸婊堝吹閵堝鈷戦柛娑橈梗缁堕亶鏌涢妸锕€鈻曢柟顔肩－缁辨帒螣閸︻厾鐣鹃梻渚€娼ч悧鍡涘磹閸涘﹦顩插Δ锝呭暞閸?
            setOnInfoWindowClickListener(this@RestaurantHomeActivity)

            // 缂傚倸鍊搁崐鎼佸磹妞嬪海鐭嗗〒姘ｅ亾閽樻繈鏌熼崜浣烘憘闁轰礁顑囬幉鍛婃償閵娿儳鐤勯梺闈浥堥弲娑㈡偂濞戙垺鐓曟繛鎴濆船楠炴ɑ銇勯敂璇蹭喊婵﹨娅ｇ槐鎺戭潨閸℃鏆ラ梻浣虹帛椤ㄥ懎螞濠靛绠栨俊顖濐嚙缁剁偤鏌熼柇锕€骞橀柛妯圭矙濮婃椽妫冨☉杈╁姼闂佺閰ｆ禍鍫曘€侀弮鍥舵Ь缂備浇椴搁幐鎼侇敇婵傜宸濇い鏂垮悑椤忋倗绱?
            setOnMyLocationButtonClickListener(this@RestaurantHomeActivity)

            // 闂傚倸鍊搁崐鐑芥嚄閸撲礁鍨濇い鏍仜缁€澶嬩繆閵堝懏鍣圭紒鐘靛█閺岀喖骞戦幇闈涙闂佸憡淇洪～澶愬Φ閸曨垰鍗虫俊銈傚亾濞存粍鍎宠灃闁绘﹢娼ф禒婊勩亜閹存繍妯€闁绘侗鍠氶埀顒婄秵閸犳宕戦幇顔瑰亾鐟欏嫭绀€婵炶绠戦埢鎾剁矙鎼存挻鏂€闂佺粯鍔橀崺鏍亹瑜忕槐鎺楁嚑椤掍焦鐝紓渚囧枟濡啫鐣锋總绋垮嵆闁绘棁娓归惀?
            setOnMyLocationClickListener(this@RestaurantHomeActivity)
            // 闂傚倸鍊风粈渚€骞栭位鍥敍閻愭潙浜遍梺绯曞墲缁嬫垹绮婚悩璇茬婵烇綆鍓欓悘顏堟煕濞嗗繒绠查柕鍥у缁犳盯骞樼捄渚毇缂傚倷璁查崑鎾愁熆閼搁潧濮堥柣鎾跺枛閺岋繝宕橀妸锔俱偐闂佹悶鍊栭悷鈺呭蓟濞戞鐔兼惞鐟欏嫭鍠栧┑鐑囩到濞层倝鏁冮鍫涒偓浣糕槈濮楀棙瀵岄柣鐘叉穿瀵挻绔熼弴銏″€甸悷娆忓缁€鈧┑鐐茬湴閸旀垿濡存笟鈧畷銊р偓娑櫱氶幏娲⒑閸︻収鐒炬繛瀵稿厴閸┿儲寰勯幇顓犲幗濡炪倖鎸荤换鍕玻閺冨牊鐓忛柛鈩冾殕閸ゅ洦銇勯姀鈩冪闁轰礁鍊块幐濠冨緞婵犲偆妫嗛梻鍌氬€风粈渚€骞栭銈囩煓濞撴埃鍋撶€规洘绮撻弫鍐焵椤掑嫧鈧箓宕堕鈧粻铏繆閵堝嫯鍏岄柛姗€浜跺娲濞戣鲸肖闂佺姘︽禍顒傛閻愬搫鍐€妞ゆ劑鍊楅敍婊冣攽鎺抽崐鎰板磻閹剧粯鐓曞┑鐘插€归崑銉︻殽閻愬弶顥㈢€规洘锕㈤、娆撴嚃閳哄﹥效濠碉紕鍋戦崐鏍偋濡ゅ啫鍨濈€光偓閳ь剛鍒掗銏℃櫢闁绘ê纾崢浠嬫⒑缂佹﹫鑰垮ù婊嗘硾椤﹪鎮滈懞銉у幈婵犵數濮撮崯顖滅矆鐎ｎ兘鍋撶憴鍕闁告挻鐩崺鈧い鎺戝€归弳鈺冪棯椤撶偟鍩ｇ€规洘鍨块獮姗€鎳滈棃娑樺箲闂備礁鎲＄划鍫ユ倿閿斿墽绀婇柛顐犲劜閸婂灚绻涢崼婵堜虎闁哄鍠栭幃妤冪箔濞戞ê鏋ら柣?
            setContentDescription("Map with lots of markers.");
        }

        googleMap.uiSettings.apply {
            isMapToolbarEnabled = true
            // 闂傚倸鍊搁崐鎼佸磹閹间礁纾归柟闂寸缁犵娀鐓崶銊р槈缂佲偓婢跺绻嗘い鏍ㄧ懅缁夋寧绻涢幋鐑嗙劷闁哄棗妫濋弻娑㈠即閵娿儰绨藉銈嗘尰濡炶棄顫忓ú顏呭仭闂侇叏绠戝▓鑸电節濞堝灝鏋ら柡浣割煼楠炲啯銈ｉ崘銊ь攨闂佽绻楅褏绱炴繝鍥ф瀬闁圭増婢樺婵囥亜閺嵮勫櫡闁稿﹥绻堝濠氭晲婢跺á褔鏌涘☉鍗炴珮婵☆偓绠撻幃妤冩喆閸曨剙顦╂繛瀛樼矌閸嬬偟绮嬮幒妤佹櫇闁稿本绋戝▓鐔兼⒑闂堟冻绱￠悘鐐插⒔閺屽牓姊?
            // isMyLocationButtonEnabled = false
        }

        // 闂傚倷娴囧畷鐢稿窗閹邦喖鍨濋幖娣灪濞呯姵淇婇妶鍛櫣缂佺姳鍗抽弻娑樷槈濮楀牊鏁惧┑鐐叉噽婵炩偓闁哄矉绲借灒闁圭娴疯ぐ褎绻濆▓鍨仩妞ゆ洦鍙冮崺鈧い鎺嗗亾缂佺姴绉瑰畷鏇㈠础閻忕粯妞芥俊鎼佸煛娴ｇ娈?
        enableMyLocation()
        presenter.getMakerData(mRequest)

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
    }


    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵炲瓨绮岀紞濠囧蓟濞戙垹唯妞ゆ梻鍘ч～鈺冪磽娴ｅ搫顎撶紓宥勭窔瀵鎮㈢悰鈥充壕闁汇垺顔栭悞楣冨冀閿熺姵鈷戝ù鍏肩懅閸掓壆鈧娲﹂崜鐔凤耿娓氣偓濮婃椽骞愭惔锝囩暤闂佺娅曢崝姗€宕愰幘顔解拻濞达絽婀卞﹢浠嬫煕婵犲啯绀€閾荤偞绻涢崱妯虹仼闁活厽宀稿娲敆閳ь剛绮旈幘顔藉€挎繛宸簼閻撴洟鏌熼弶鑳闁硅櫕鍔欒棟?
     */
    private fun getMyLocationInfo() {
        LocationUtil.getInstance().addressCallback = object : LocationUtil.AddressCallback {
            override fun onGetAddress(address: Address) = Unit

            override fun onGetLocation(lat: Double, lng: Double) {
                CacheUtil.getShareDefault().put(Constant.SP_MY_LATITUDE, lat.toFloat())
                CacheUtil.getShareDefault().put(Constant.SP_MY_LONGITUDE, lng.toFloat())

                myLocation = CameraPosition.Builder().target(LatLng(lat, lng))
                    .zoom(20f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        if (!::myLocation.isInitialized) return false
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(myLocation))
        return false
    }

    override fun onMyLocationClick(location: Location) {
//        ToastUtils.showShort("Current location : \nlatitude:${location.latitude}" + "\nlongitude:${location.longitude}  ")
    }

    override fun onInfoWindowClick(marker: Marker) = Unit

    override fun onMarkerClick(marker: Marker): Boolean {

        // 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉查埀顒€鍊圭粋鎺斺偓锝庝簽閿涙盯姊洪悷鏉库挃缂侇噮鍨堕幃?maker 闂傚倸鍊峰ù鍥х暦閸偅鍙忕€规洖娲ㄩ惌鍡椕归敐鍫綈婵炲懐濮撮湁闁绘ê妯婇崕鎰版煕鐎ｅ吀閭柡灞剧洴閸╁嫰宕橀浣诡潔缂傚倷鑳舵刊瀵哥礊娓氣偓瀵鎮㈤崗鑲╁姺闂佹寧娲嶉崑鎾搭殽閻愭惌娈橀柍褜鍓氶鏍窗濡ゅ懎绠查柛銉戝本缍庡┑鐐叉▕娴滃爼寮崘顔界厪闊洤锕ュ▍鍡涘疮閹间焦鈷?
        makers.forEach {
            val makerTag = it?.tag as RestaurantResponse
            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
            val title_tv = view.findViewById<TextView>(R.id.title_tv)
            title_tv.text = makerTag.title
            val image_iv = view.findViewById<ImageView>(R.id.image_iv)
            image_iv.setImageResource(R.mipmap.ic_marker_common_food)

            it.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        }
        marker.hideInfoWindow()

        // 闂傚倸鍊峰ù鍥х暦閸偅鍙忕€规洖娲ㄩ惌鍡椕归敐鍫綈婵炲懐濮撮湁闁绘ê妯婇崕鎰版煕鐎ｅ吀閭柡灞剧洴閸╁嫰宕橀妸锔藉劎濠电偛鐡ㄧ划宀€绱炴繝鍥ц摕闁绘梻鍘х粻姘辨喐瀹ュ姹查弶鍫涘妸娴滄粓鏌曢崼婵囧櫤闁搞倕娲﹂幈銊︾節閸愨斂浠㈠銈冨灪濞茬喐鎱ㄩ埀顒勬煃閸濆嫬鈧劙濡烽埡鍌楁嫼?
        val currentMakerTag = marker.tag as RestaurantResponse
        val view = LayoutInflater.from(this).inflate(R.layout.custom_info_contents_2, null)
        val title_tv = view.findViewById<TextView>(R.id.title_tv)
        title_tv.text = currentMakerTag.title
        val image_iv = view.findViewById<ImageView>(R.id.image_iv)
        image_iv.setImageResource(R.mipmap.ic_marker_select_food)
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(ConvertUtils.view2Bitmap(view)))

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒佹闂佸湱鍎ら崵姘洪鍛珖闂侀€炲苯澧撮柟顕€绠栭幃婊堟寠婢跺瞼鍘┑鐘灱濞夋盯顢栭崨鏉戠闁告稑鐡ㄩ埛鎴︽煕濠靛棗顏柛灞诲姂閺屾盯濡搁妷褏楔閻庤娲樼换鍫熶繆閻戣棄鐓涢柛灞剧矊楠炲牓姊绘担鍛婅础闁稿簺鍊濋獮鏍煛娴ｅ弶鐏佸┑鐐叉閹稿鎮¤箛娑欑厱闁斥晛鍠氬▓鏃堟煛閸☆厾鎮奸柍褜鍓氶鏍闯椤曗偓瀹曟垶绻濋崶褏鐣洪悷婊勬煥閻ｇ兘鎮℃惔妯绘杸闂佸壊鍋呯粙鎴炵?
        val index = makers.indexOf(marker)
        if (index != -1) {
            binding.bottomSheetRv.scrollToPosition(index)
            makerBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // 闂傚倸鍊搁崐椋庢濮橆剦鐒藉┑鐘崇閳锋棃鏌涢弴銊ヤ航闁绘柨妫欐穱濠囶敍濮樸儱浠洪梺鑽ゅ枛閸嬪﹤顭囬妸鈺傜厓鐟滄粓宕滈悢鐓庣畺濞村吋鎯岄弫瀣煃瑜滈崜鐔肩嵁閺嶎兙浜归柟鐑樼箖閺呮繈姊洪棃娑氬婵☆偅顨婇、鏃堝Χ閸モ晝锛濋梺绋挎湰閻熝囁囬敂濮愪簻闁挎棁妫勯埢鏇熴亜閵忊€蹭孩妞わ附濞婇弻鐔哥瑹閸喖顫囬梺绯曟櫔缁绘繂鐣烽妸鈺婃晣闁绘瑢鍋撴俊鍙夋尦濮婄粯鎷呯粵瀣秷濡炪倖姊归悧鐘茬暦閺夎鏃堝礃椤忓棗绠垫繝寰锋澘鈧洟骞婅箛娑欏亗婵炴垶鍩冮崑鎾荤嵁閸喖濮庨梺鐟板暱缁绘﹢骞嗙仦杞挎棃宕橀鍡闯闁诲骸绠嶉崕閬嶅箠韫囨稑闂柣锝呯灱绾?
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


}
