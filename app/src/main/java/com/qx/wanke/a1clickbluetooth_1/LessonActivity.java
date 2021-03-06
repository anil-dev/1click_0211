package com.qx.wanke.a1clickbluetooth_1;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import jaydenxiao.com.expandabletextview.ExpandableTextView;

public class LessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lesson);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("一键蓝牙的使用说明");

        ExpandableTextView mText0=(ExpandableTextView)findViewById(R.id.etv0);
        ExpandableTextView mText1=(ExpandableTextView)findViewById(R.id.etv1);
        ExpandableTextView mText2=(ExpandableTextView)findViewById(R.id.etv2);
        ExpandableTextView mText3=(ExpandableTextView)findViewById(R.id.etv3);
        ExpandableTextView mText4=(ExpandableTextView)findViewById(R.id.etv4);
        ExpandableTextView mText5=(ExpandableTextView)findViewById(R.id.etv5);
        ExpandableTextView mText6=(ExpandableTextView)findViewById(R.id.etv6);
        ExpandableTextView mText7=(ExpandableTextView)findViewById(R.id.etv7);


        mText0.setText("        “一键蓝牙”是一款轻应用，把你蓝牙耳机/音箱的连接和随后的app开启集成在一个界面里，可以让你更加流畅的用蓝牙听歌、导航、微信聊天或者打电话。她体积小、目标明确，没有后台驻留、没有自作主张的各种自启动，即用即开，关闭即走。\n" +
                "        她适合于经常使用蓝牙耳机、音箱或车载蓝牙的朋友，尤其是日常要用到几个蓝牙设备时常切换的人。");

        mText1.setText("        我很喜欢听歌，但超不喜欢有线耳机、音箱那些总是弄得乱糟糟的音频线，所以买了不少蓝牙的设备。平时听歌打电话用的耳机、给宝宝听故事的音箱、出去踏青带的便携音箱、去锻炼时候用的运动耳机、车载的蓝牙……这些蓝牙设备的确让我播放的时候方便了很多，但每次接入不同的设备却成了一件烦心的事儿。\n" +
                "        android手机打开蓝牙，需要进入设置-蓝牙-打开-连接相应设备（如果你在桌面放一个widget，能稍微方便一些），每次切换个设备，都得折腾一下，然后再回桌面找到需要的app（音乐的、视频的、喜马拉雅这样的音频故事类的或者导航类的），有时候空闲下来想听个10分钟的音乐，却又觉得折腾一圈特繁琐。\n" +
                "        为什么蓝牙设备这么普及，蓝牙5.0都面世了，可是却没有一个方便的app把这些操作整合起来呢？千年不变的都是要进系统的设置里去开。既然我这么想，一定也有和我一样喜欢听歌、喜欢用蓝牙的朋友，也会觉得这样很麻烦。想想干脆自己来做一个app，给我们这些人一个稍微方便智能些的解决方案。\n");
        mText2.setText("        1、蓝牙设备的顺序不能调整，如果有个8、9个蓝牙设备的话，每次都要在设置界面里上下翻查一下；\n" +
                "        2、图标单调不能更换，永远都是一个耳机的图标，连接的时候要仔细看清楚设备名称才不会弄错。我总想，如果给孩子听睡前故事要连接她的蓝牙音箱的时候，能够用宝宝的头像做她的小兔音箱的标志，这样孩子都能一眼找到，方便的连接好；\n" +
                "        3、上面提到的，连接蓝牙设备和打开需要的app不在一个界面里，来回切换挺折腾；\n" +
                "        4、连接以后，系统的状态栏里只有一个蓝牙标志，如果身边有两个设备（比如开车的时候，车载蓝牙和随身戴着的蓝牙耳机），到底连接的是谁可能搞不清楚；\n" +
                "        5、蓝牙耳机很多都有分别连接音乐流和电话音频的功能，但如果你想单独连接这个设备的其中一个子功能，就会更加繁琐（要在系统的蓝牙设置里，再进入设备单独的项目里，关闭其中不需要的子功能）。比如你在车上，可能需要车载的蓝牙播放手机里的导航或者音乐，而来电话时，并不需要用车载的音箱外放出来给大家都听到，而希望这时候的电话音频子功能能单独连接到自己的蓝牙耳机里。或者你在给宝宝用蓝牙音箱听手机喜马拉雅app里的故事时，来电话时也会需要直接用手机接听，而不是外放出来，让通话被宝宝兴奋的大喊大叫打断。这时候单独连接音频或电话子功能的需求就很有必要了。这个单独连接一个蓝牙设备的音频或手机的子功能，我查找了安卓和苹果的各个app商城，貌似全世界迄今为止，只有“一键蓝牙”做的最方便，哈哈（手动傲娇脸）。");
        mText3.setText("        第一次打开app的时候，点击屏幕下方的“开关蓝牙”按钮，等系统的蓝牙打开后，你以前配对过的蓝牙设备就出现在上方的蓝牙列表里，如果设备比较多的话，你可以左右滑动这个列表，找到你需要的设备。\n" +
                "        （注意：有些手机可以直接打开蓝牙，有些手机系统会增加一个管理界面，询问你是否要打开蓝牙，如果你不喜欢每次都要点击“同意”这个步骤，可以进入手机的 设置-应用管理-授权管理-应用权限管理 的界面（不同的手机，可能进入的步骤不会完全一致，找到应用权限管理界面就好），找到“一键蓝牙”，然后把它的“开启蓝牙”权限设为同意，以后就不会有这个弹窗了）\n" +
                "        点击 设备图标，如果这个蓝牙耳机在身边，并且已经打开了，“一键蓝牙”就可以帮你连接它了；\n" +
                "        长按 后可以左右拖拽这个图标，放到列表里你想放置的位置，方便你以后在你喜欢的位置点击它；\n" +
                "        下滑 图标可以进入这个设备的详细信息的设置页面，你可以给它设置一个你喜欢的图标，起一个你方便识别的名称（也可以很方便的恢复这个设备出厂的名称），如果你的耳机（或者音箱）有分别支持音频流和电话音频的功能，你可以在设置页面下方的选择框里勾选相应的选项，这样就能方便的实现手机单独连接耳机（音箱）的音频或者电话的功能了。（有些音箱，比如我的小米音箱只支持音频流，那就不要勾选电话选项，以免把自己绕糊涂了）；\n" +
                "        全部设置好以后，点右侧那个圆形带提勾的按钮，你的设备的个性化设置就搞定了。\n" +
                "        这时，你可以在app的主界面里点击设备的图标，来连接或断开相应设备的蓝牙连接，也可以点击图标下面的“音频”或者“电话”这两个小控件，来分别连接或者断开这个设备连接相应通道的子功能。\n" +
                "\n" +
                "        需要启动的app列表刚开始时也是空的，你可以在主界面下方的输入框里，输入app的名称（全称或者其中连续的几个字），点旁边的提交，“一键蓝牙”就能帮你找到本机里所有含这几个字的app，并把它们加入到上方的app列表里。\n" +
                "        点击 图标，是直接启动相应的app；\n" +
                "        长按 图标进行左右拖拽，把你要启动的app位置排列到你觉得最合适的位置；\n" +
                "        下滑 图标，是把这个app从列表里删除掉。\n" +
                "        有个要注意的事儿，如果你要把打电话加到列表里，打电话这个功能在我们的android手机里，能看到一个“电话”、和一个“通讯录与拨号”的图标，但有些手机按这两个名字搜，根本搜不到。试验了多部手机，发现这个app在系统里的名字有可能是”电话“、”通讯录与拨号“或者”联系人“，试试分别按这三个名称搜，基本就确定能找到这款系统的app了。\n" +
                "        全部设置好以后，你的界面可能就像下面这样：\n" +
                "\n" +
                "        以后再使用蓝牙的时候，就不必在系统设置、桌面这些页面之间来回穿梭，只要打开“一键蓝牙”就能方便搞定了。给使用蓝牙带来很多方便，我现在已经把“一键蓝牙”放在手机桌面最趁手的下方启动栏里，随时使用的感觉很爽。也很希望能给你的使用带来一些便捷。");

        mText4.setText("        “一键蓝牙”是对连接蓝牙设备做的便捷处理，并不做配对蓝牙的工作。所以你新买的蓝牙耳机、蓝牙音箱等，要先在系统蓝牙设置界面里按照设备说明书上的要求，先做配对处理，成为了手机蓝牙的配对设备以后，才能在“一键蓝牙”里看见它。配对工作是一次性的，不像连接设备需要每次听歌前都要做，所以这种单趟耗时的事情，我就没有集成到app里面，还是交给系统蓝牙设置来做比较合理。\n" +
                "        “一键蓝牙”更像是封装在系统蓝牙外层的一个直观轻巧的控制中枢，软件里使用的连接协议都是android公开的a2dp和headset协议，与“系统蓝牙设置”是平级的权限,没有对手机系统的底层做任何更改。所以当你遇到偶尔点击设备却没法连接的时候（我碰到过几次），不要担心是“一键蓝牙”破坏了你android的底层，你可以进入系统蓝牙设置的界面（用“一键蓝牙”左下角的“系统蓝牙设置”按钮）观察一下，是不是在那里也没法连接这个设备，记得有一次，我连接一个已经配对过的蓝牙音箱，在app和系统设置里，都死活没法连接，最后只好在系统蓝牙设置里删掉这个设备，过一个小时以后，再重新搜索它，重新配对，然后又一切正常了。\n" +
                "\n" +
                "        手机蓝牙打开的时候，系统会有一个自动连接的动作，如果这时候手边有已经开启的蓝牙设备，是可能没经过我们点击就自动连接的，因为“一键蓝牙”这款app不对系统底层做任何更改，所以这个系统自动连接过程我并不去干预，如果连接的是你需要连接的设备，那么就已经实现目的了；如果不是的话，我们也能很方便的在“一键蓝牙”里或者断开它，或者重新连接需要的设备。\n" +
                "        如果你刚刚连上设备，就再次点击图标要断开连接，系统的蓝牙往往会断开后再次帮你重新连接，所以如果你刚连接突然又想断开，那可以稍等一会，再去点击断开。\n" +
                "        另外，如果你单独点击了“音频”或者“电话”，在连接后，android系统有时候会自作聪明的把另一个协议也连接上，这时候，只好再稍等几秒，再单独点击不需要的那个协议，让它断开。比较方便的做法是：比如你在车上，点击车载蓝牙连接了音频，如果android系统自动让车载也连接了电话，你稍等一下，点击你耳机的电话子栏目，就能直接断开车载的电话连接，同时连接了你的耳机。\n");

        mText5.setText("        这个app虽然很小，但从立项到这个app真实的来到你我的手机里，也花费了不少业余时间，过程中有遇到奇怪问题时的辗转反侧，也有豁然开朗时候的欢喜雀跃。有一次从夜里10点多开始撸代码，一口气写到凌晨2点，3、400行代码里各种判断、跳转，写好后竟然一次编译通过，手机里跑的完全正确，让我兴奋的满屋转圈，想找人显摆，可是妻和宝都早已呼呼大睡，实在没办法，只好来到里屋，抱着宝宝的熟睡的小胖脸，狠狠的亲了一口。\n" +
                "\n" +
                "        平时使用各种app，比较反感的就是各种自作主张的后台自启动、弹窗，或者申请一堆侵犯隐私的权限，见过某个让孩子玩拼图游戏的app都要申请”读取联系人“、”读取通话记录“、”定位“等等各种权限。所以，自己写的这个”一键蓝牙“，没有申请一个多余的权限，app里明确申请的只有“开启蓝牙”（这个不用解释）、“读写手机存储”（这个权限是为了在设置蓝牙细节时，有个功能是从相册里选图片做设备的头像）和“后台弹出界面”（这是为了你在使用蓝牙过程中，app能做一些当前设备连接或断开的一些提示）。有些特定的机型（比如我的小米），会自动增加“发送彩信”（所有的app都有这个，让人觉得奇怪），“锁屏显示”。这些都和“一键蓝牙”无关。在这方面，虽然仅仅是个人做的app，但比某些互联网大企业做的要有节操一点，对别人隐私多了一些尊重，还怪值得表扬自己一下的。吼吼。\n");

        mText6.setText("        虽然“一键蓝牙”经过了我和我的朋友们的多次测试，但因为android的碎片化一直是适配工作的大难题，所以，难免在你的手机上会出现一些问题，如果有什么意见或者建议，可以联系我（邮箱或微信号见下面），主题里注明“一键蓝牙 交流”。\n" +
                "        如果你是蓝牙耳机或者音箱的生产厂商的话，如果需要在这款app上精准投放广告也可以联系我，主题里注明“一键蓝牙 商务\"。\n" +
                "        在“一键蓝牙”里投放设备广告的好处是：喜欢使用这款app的朋友，应该都对相关蓝牙产品比较感兴趣，同时这款app的界面正好只需要手机屏幕的下半段做操作（也是考虑单手操作的便利性），上半部分的图片区如果是一些比较精美的产品介绍，不影响app的使用，我想大家可能也不会那么反感，偶尔也会有兴趣点开看一看。\n" +
                "        原本是打算做收费软件的，但后来想想，如果有更多的人能用到这款app，而且能获得一些方便，可能给我的鼓励和成就感会更多，所以还是决定把“一键蓝牙”做成一款免费软件。如果你觉得喜欢这款app，感觉给你带来一些便捷的话，我一样会觉得很高兴。\n" +
                "\n" +
                "        联系方式：邮箱 1click@sina.com 微信号 a-click （发邮件或加好友请注明“一键蓝牙 交流”或“一键蓝牙 商务”）");

        mText7.setText("        一款app，从念头的初生到产品的落地，中间经历了很多困难，克服这些困难并实现了功能也带给我很多的快乐。很想感谢很多人的帮助，虽然有些是素未谋面的朋友：\n" +
                "        android开发界的郭霖大神写的《第一行代码（第二版）》对我的帮助很大，一本编程书籍能写的如此清晰流畅，可以想象的到作者扎实的技术功底和良好的思维习惯。\n" +
                "        为了不重复制作轮子，这个app里用到了一些很好的开源库，感谢 hdodenhof 制作的 CircleImageView，世锋日上 制作的轻巧美观的 ExpandableTextView。\n" +
                "\n" +
                "        谢谢我亲爱的妻子和宝宝，作为我的专职饲养员，静同学确保了我在没日没夜写代码的日子里，没有成为营养不良的灾民；宝宝虽然不知道爸爸在忙啥，还让爸爸陪她要到22点以后才能开始专心工作，但一起玩耍带给我的快乐，是和坐在电脑前敲键盘完全不同的另一种满足。这款app是我送给你们的一个小小礼物，尽管你们一个用的是ios的手机，一个用的还是塑料的玩具手机。:-)\n" +
                "                                                      ——作者anil于2017.3.31晚");


    }
}
