
/*
 * SpinalHDL
 * Copyright (c) Dolu, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package spinal.core

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Stack}

case class BitCount(val value: Int) {
  def +(right : BitCount) = BitCount(this.value + right.value)
  def -(right : BitCount) = BitCount(this.value - right.value)
  def *(right : BitCount) = BitCount(this.value * right.value)
  def /(right : BitCount) = BitCount(this.value / right.value)
  def %(right : BitCount) = BitCount(this.value % right.value)
}
case class SlicesCount(val value: Int) {
}

case class ExpNumber(val value: Int) {}
case class PosCount(val value: Int) {}

object CyclesCount{
  implicit def impConv(value : CyclesCount) = value.value
}
case class CyclesCount(val value : BigInt)

object PhysicalNumber{
//  implicit def impConv(value : PhysicalNumber[_]) = value.value
//  implicit def impConv(value : PhysicalNumber[_]) = value.value.toInt
//  implicit def impConv(value : PhysicalNumber[_]) = value.value.toLong
//  implicit def impConv(value : PhysicalNumber[_]) = value.value.toFloat
//  implicit def impConv(value : PhysicalNumber[_]) = value.value.toDouble
}

abstract class PhysicalNumber[T <: PhysicalNumber[_]](protected val value : BigDecimal) {
  def newInstance(value : BigDecimal) : T

  def +(that : T) = newInstance(this.value + that.value)
  def -(that : T) = newInstance(this.value - that.value)
//  def *(that : T) = newInstance(this.value * that.value)
//  def /(that : T) = newInstance(this.value / that.value)
//  def %(that : T) = newInstance(this.value % that.value)

//  def +(that : PhysicalNumber) = (this.value + that.value)
//  def -(that : PhysicalNumber) = (this.value - that.value)
  def *(that : PhysicalNumber[_]) = (this.value * that.value)
  def /(that : PhysicalNumber[_]) = (this.value / that.value)
  def %(that : PhysicalNumber[_]) = (this.value % that.value)

  def +(that : BigDecimal) = newInstance(this.value + that)
  def -(that : BigDecimal) = newInstance(this.value - that)
  def *(that : BigDecimal) = newInstance(this.value * that)
  def /(that : BigDecimal) = newInstance(this.value / that)
  def %(that : BigDecimal) = newInstance(this.value % that)

  def max(that : T) = newInstance(value.max(that.value))

  def toInt = value.toInt
  def toLong = value.toLong
  def toDouble = value.toDouble
  def toBigDecimal = value
}

case class TimeNumber(private val v : BigDecimal) extends PhysicalNumber[TimeNumber](v){
  override def newInstance(value: BigDecimal): TimeNumber = TimeNumber(value)

  def +(that : HertzNumber) = (this.value + that.toBigDecimal)
  def -(that : HertzNumber) = (this.value - that.toBigDecimal)
  def *(that : HertzNumber) = (this.value * that.toBigDecimal)
  def /(that : HertzNumber) = (this.value / that.toBigDecimal)
  def %(that : HertzNumber) = (this.value % that.toBigDecimal)

  def toHertz = HertzNumber(1/this.value)

  def decompose: (BigDecimal,String) = {
    if(value > 3600.0) return (value/3600.0,"hr")
    if(value > 60.0) return (value/60.0,"min")
    if(value > 1.0) return (value/1.0,"sec")
    if(value > 1.0e-3) return (value/1.0e-3,"ms")
    if(value > 1.0e-6) return (value/1.0e-6,"us")
    if(value > 1.0e-9) return (value/1.0e-9,"ns")
    if(value > 1.0e-12) return (value/1.0e-12,"ps")
    (value/1.0e-15,"fs")
  }
}

case class HertzNumber(private val v : BigDecimal) extends PhysicalNumber[HertzNumber](v){
  override def newInstance(value: BigDecimal): HertzNumber = HertzNumber(value)

  def +(that : TimeNumber) = (this.value + that.toBigDecimal)
  def -(that : TimeNumber) = (this.value - that.toBigDecimal)
  def *(that : TimeNumber) = (this.value * that.toBigDecimal)
  def /(that : TimeNumber) = (this.value / that.toBigDecimal)
  def %(that : TimeNumber) = (this.value % that.toBigDecimal)

  def toTime = TimeNumber(1/this.value)
}




trait IODirection extends BaseTypeFactory {
  def applyIt[T <: Data](data: T): T
  def apply[T <: Data](data: T): T = applyIt(data)
  def apply[T <: Data](datas: T*): Unit = datas.foreach(applyIt(_))
  def apply(enum: SpinalEnum) = applyIt(enum.craft())
  def cloneOf[T <: Data](that: T): T = applyIt(spinal.core.cloneOf(that))
  def apply(b : Int) = 10
  override def Bool = applyIt(super.Bool)
  override def Bits = applyIt(super.Bits)
  override def UInt = applyIt(super.UInt)
  override def SInt = applyIt(super.SInt)
  override def Vec[T <: Data](elements: TraversableOnce[T]): Vec[T] = applyIt(super.Vec(elements))



  //  object Vec extends VecFactory {
  //    override def apply[T <: Data](elements: TraversableOnce[T]): Vec[T] = applyIt(super.apply(elements))
  //  }
  override def postTypeFactory[T <: Data](that: T): T = applyIt(that)
}

object in extends IODirection {
  override def applyIt[T <: Data](data: T): T = data.asInput()
}

object out extends IODirection {
  override def applyIt[T <: Data](data: T): T = data.asOutput()
}

object inWithNull extends IODirection {
  override def applyIt[T <: Data](data: T): T = if(data != null) data.asInput() else data
}

object outWithNull extends IODirection {
  override def applyIt[T <: Data](data: T): T = if(data != null) data.asOutput() else data
}

//object IntBuilder {
//  implicit def IntToBuilder(value: Int) = new IntBuilder(value)
//}
//
//case class IntBuilder(i: Int) {
//  def bit = new BitCount(i)
//}


trait MinMaxProvider {
  def minValue: BigInt
  def maxValue: BigInt
}


trait ContextUser extends GlobalDataUser {
  var component = Component.current(globalData)
  private[core] var conditionalAssignScope = globalData.conditionalAssignStack.head()
  private[core] var instanceCounter = globalData.getInstanceCounter

  def getInstanceCounter = instanceCounter
  private[core] def isOlderThan(that : ContextUser) : Boolean = this.instanceCounter < that.instanceCounter
}

trait GlobalDataUser {
  val globalData = GlobalData.get
}

trait NameableByComponent extends Nameable with GlobalDataUser{
  override def getName(): String = {
    if(!globalData.nodeAreNamed) {
      if (isUnnamed) {
        val c = getComponent()
        if(c != null)c.nameElements()
      }
    }
    return super.getName()
  }

  private[core] def getComponent() : Component
}

object SyncNode {
  val getClockInputId: Int = 0
  val getClockEnableId: Int = 1
  val getClockResetId: Int = 2
  val getClockSoftResetId: Int = 3
}

abstract class SyncNode(_clockDomain: ClockDomain = ClockDomain.current) extends Node {
  var clockDomain: ClockDomain = _clockDomain
  def setClockDomain(clockDomain: ClockDomain) : this.type = {
    this.clockDomain = clockDomain
    this
  }

  var clock      : Bool = null
  var enable     : Bool = null
  var reset      : Bool = null
  var softReset  : Bool = null


  override def onEachInput(doThat: (Node, Int) => Unit): Unit = {
    doThat(clock,SyncNode.getClockInputId)
    if(isUsingEnableSignal)doThat(enable,SyncNode.getClockEnableId)
    if(isUsingResetSignal) doThat(reset,SyncNode.getClockResetId)
    if(isUsingSoftResetSignal) doThat(softReset,SyncNode.getClockSoftResetId)
  }
  override def onEachInput(doThat: (Node) => Unit): Unit = {
    doThat(clock)
    if(isUsingEnableSignal)doThat(enable)
    if(isUsingResetSignal) doThat(reset)
    if(isUsingSoftResetSignal) doThat(softReset)
  }

  override def setInput(id: Int, node: Node): Unit = id match{
    case SyncNode.getClockInputId => clock = node.asInstanceOf[Bool]
    case SyncNode.getClockEnableId if(isUsingEnableSignal) => enable = node.asInstanceOf[Bool]
    case SyncNode.getClockResetId if(isUsingResetSignal)  => reset = node.asInstanceOf[Bool]
    case SyncNode.getClockSoftResetId if(isUsingSoftResetSignal)  => softReset = node.asInstanceOf[Bool]
  }

  override def getInputsCount: Int = 1 + (if(isUsingEnableSignal) 1 else 0) + (if(isUsingResetSignal) 1 else 0) + (if(isUsingSoftResetSignal) 1 else 0)
  override def getInputs: Iterator[Node] = {
    val itr = (isUsingEnableSignal,isUsingResetSignal) match{
      case (false,false) => Iterator(clock             )
      case (false,true)  => Iterator(clock,       reset)
      case (true,false)  => Iterator(clock,enable      )
      case (true,true)   => Iterator(clock,enable,reset)
    }
    if(isUsingSoftResetSignal)
      itr ++ List(softReset)
    else
      itr
  }

  override def getInput(id: Int): Node = id match{
    case SyncNode.getClockInputId => clock
    case SyncNode.getClockEnableId if(isUsingEnableSignal) => enable
    case SyncNode.getClockResetId if(isUsingResetSignal)  => reset
    case SyncNode.getClockSoftResetId if(isUsingSoftResetSignal)  => softReset
  }


  override private[core] def getOutToInUsage(inputId: Int, outHi: Int, outLo: Int): (Int, Int) = inputId match{
    case SyncNode.getClockInputId =>  (0,0)
    case SyncNode.getClockEnableId => (0,0)
    case SyncNode.getClockResetId =>  (0,0)
    case SyncNode.getClockSoftResetId =>  (0,0)
  }

  final def getLatency = 1 //if not final => update latencyAnalyser

  def getSynchronousInputs = {
    var ret : List[Node] = Nil
    if(clockDomain.config.resetKind == SYNC  && isUsingResetSignal) ret = getResetStyleInputs ++ ret
    if(isUsingEnableSignal) ret = getClockEnable :: ret
    if(isUsingSoftResetSignal) ret = getSoftReset :: ret
    ret
  }

  def getAsynchronousInputs : List[Node] = (if (clockDomain.config.resetKind == ASYNC && isUsingResetSignal) getResetStyleInputs else Nil)

  def getResetStyleInputs = List[Node](getReset)

  def isUsingSoftResetSignal : Boolean
  def isUsingResetSignal: Boolean
  def isUsingEnableSignal: Boolean = clockDomain.clockEnable != null
  def setUseReset = {
    reset = clockDomain.reset
  }
  def getClockDomain: ClockDomain = clockDomain

  def getClock: Bool = clock
  def getClockEnable: Bool = enable
  def getReset: Bool = reset
  def getSoftReset: Bool = softReset
}

trait Assignable {
  /* private[core] */var compositeAssign: Assignable = null

  /*private[core] */final def assignFrom(that: AnyRef, conservative: Boolean): Unit = {
    if (compositeAssign != null) {
      compositeAssign.assignFrom(that, conservative)
    } else {
      assignFromImpl(that, conservative)
    }
  }

  private[core] def assignFromImpl(that: AnyRef, conservative: Boolean): Unit
}

object OwnableRef{
  def set(ownable : Any,owner : Any) = {
    if(ownable.isInstanceOf[OwnableRef])
      ownable.asInstanceOf[OwnableRef].setRefOwner(owner)
  }
  def proposal(ownable : Any,owner : Any) = {
    if(ownable.isInstanceOf[OwnableRef]) {
      val ownableTmp = ownable.asInstanceOf[OwnableRef]
      if(ownableTmp.refOwner == null)
        ownableTmp.asInstanceOf[OwnableRef].setRefOwner(owner)
    }
  }
}

trait OwnableRef{
  type RefOwnerType
  @dontName var refOwner : RefOwnerType = null.asInstanceOf[RefOwnerType]
  def setRefOwner(that : Any): Unit ={
    refOwner = that.asInstanceOf[RefOwnerType]
  }

  def getRefOwnersChain() : List[Any] = {
    refOwner match {
      case null => Nil
      case owner : OwnableRef => owner.getRefOwnersChain() :+ owner
      case _ => refOwner :: Nil
    }
  }
}

object Nameable{
  val UNANMED : Byte = 0
  val ABSOLUTE : Byte = 1
  val NAMEABLE_REF : Byte = 2
  val OWNER_PREFIXED : Byte = 3
  val NAMEABLE_REF_PREFIXED : Byte = 4
}

trait Nameable extends OwnableRef{
  import Nameable._
  private var name : String = null
  private var nameableRef : Nameable = null
  private var mode : Byte = UNANMED
  private var weak : Byte = 1

  private def getMode = mode
  private[core] def isWeak = weak != 0
  private[core] def setMode(mode : Byte)     = this.mode = mode
  private[core] def setWeak(weak : Boolean) = this.weak = (if(weak) 1 else 0)

  def isUnnamed: Boolean = getMode match{
    case UNANMED => true
    case ABSOLUTE => name == null
    case NAMEABLE_REF => nameableRef == null || nameableRef.isUnnamed
    case NAMEABLE_REF_PREFIXED => nameableRef == null || nameableRef.isUnnamed || name == null
    case OWNER_PREFIXED => refOwner == null || refOwner.asInstanceOf[Nameable].isUnnamed
  }
  def isNamed: Boolean = !isUnnamed

  def getName() : String = getName("")
  def getName(default : String): String = getMode match{
    case UNANMED => default
    case ABSOLUTE => name
    case NAMEABLE_REF => if(nameableRef != null && nameableRef.isNamed) nameableRef.getName() else default
    case NAMEABLE_REF_PREFIXED => if(nameableRef != null && nameableRef.isNamed) nameableRef.getName() + "_" + name else default
    case OWNER_PREFIXED => {
      if(refOwner != null) {
        val ref = refOwner.asInstanceOf[Nameable]
        if (ref.isNamed) {
          val ownerName = ref.getName()
          if(ownerName != "" && name != "")
            ownerName + "_" + name
          else
            ownerName + name
        } else {
          default
        }
      } else default
    }
  }


  def getDisplayName() : String = {
    val name = getName()
    if(name.length == 0)
      "???"
    else
      name
  }



  override def toString(): String = name

  private[core] def getNameElseThrow: String = getName(null)

  def setCompositeName(nameable: Nameable) : this.type  = setCompositeName(nameable,false)
  def setCompositeName(nameable: Nameable,weak : Boolean) : this.type = {
    if (!weak || (mode == UNANMED)) {
      nameableRef = nameable
      name = null
      setMode(NAMEABLE_REF)
      setWeak(weak)
    }
    this
  }

  def setCompositeName(nameable: Nameable,postfix : String) : this.type  = setCompositeName(nameable,postfix,false)
  def setCompositeName(nameable: Nameable,postfix : String,weak : Boolean) : this.type = {
    if (!weak || (mode == UNANMED)) {
      nameableRef = nameable
      name = postfix
      setMode(NAMEABLE_REF_PREFIXED)
      setWeak(weak)
    }
    this
  }

  def setPartialName(owner : Nameable,name: String) : this.type = setPartialName(owner,name,false)
  def setPartialName(name: String) : this.type = setPartialName(name,false)


  def setPartialName(owner : Nameable,name: String,weak : Boolean) : this.type = {
    if (!weak || (mode == UNANMED)) {
      setRefOwner(owner)
      this.name = name
      setMode(OWNER_PREFIXED)
      setWeak(weak)
    }
    this
  }


  def setPartialName(name: String,weak : Boolean) : this.type = {
    if (!weak || (mode == UNANMED)) {
      this.name = name
      setMode(OWNER_PREFIXED)
      setWeak(weak)
    }
    this
  }

  def unsetName() : Unit = setMode(Nameable.UNANMED)
  def setName(name: String, weak: Boolean = false): this.type = {
    if (!weak || (mode == UNANMED)) {
      this.name = name
      setMode(ABSOLUTE)
      setWeak(weak)
    }
    this
  }

  def setWeakName(name: String) : this.type = setName(name, true)

  def forEachNameables(doThat : (Any) => Unit) : Unit = {
    Misc.reflect(this, (name, obj) => {
      doThat(obj)
    })
  }
}

object ScalaLocated {
  var unfiltredFiles = mutable.Set[String](/*"SpinalUtils.scala"*/)
  var filtredFiles = mutable.Set[String]()
  //var unfiltredPackages = mutable.Set("spinal.code.","spinal.bug.","spinal.scalaTest.")

  def filterStackTrace(that : Array[StackTraceElement]) = that.filter(trace => {
    val className = trace.getClassName
    !(className.startsWith("scala.") || className.startsWith("spinal.core")) || ScalaLocated.unfiltredFiles.contains(trace.getFileName)
  })
  def short(scalaTrace : Throwable) : String = {
    if(scalaTrace == null) return "???"
    filterStackTrace(scalaTrace.getStackTrace)(0).toString
  }


  def long(scalaTrace : Throwable,tab : String = "    "): String = {
    if(scalaTrace == null) return "???"
    def filter(that : String) : Boolean = {
      if(that.startsWith("sun.reflect.NativeConstructorAccessorImpl.newInstance")) return false
      if(that.startsWith("sun.reflect.DelegatingConstructorAccessorImpl.newInstance")) return false
      if(that.startsWith("java.lang.reflect.Constructor.newInstance")) return false
      if(that.startsWith("java.lang.Class.newInstance")) return false
      if(that.startsWith("sun.reflect.NativeMethodAccessorImpl.invoke")) return false
      if(that.startsWith("sun.reflect.DelegatingMethodAccessorImpl.invoke")) return false
      if(that.startsWith("java.lang.reflect.Method.invoke")) return false
      if(that.startsWith("com.intellij.rt.execution.application.AppMain.main")) return false

      return true
    }

    filterStackTrace(scalaTrace.getStackTrace).map(_.toString).filter(filter).map(tab + _ ).mkString("\n") + "\n\n"
  }

  def short: String = short(new Throwable())
  def long: String = long(new Throwable())
}


//class SpinalMessage{
//  private val nodes = ArrayBuffer[Node]()
//  private val components = ArrayBuffer[Component]()
//  private var message : String = null
//}

trait ScalaLocated extends GlobalDataUser {
  private[core] var scalaTrace = if (globalData != null && !globalData.scalaLocatedEnable) null else new Throwable()

  def getScalaLocationLong: String = ScalaLocated.long(scalaTrace)
  def getScalaLocationShort: String = ScalaLocated.short(scalaTrace)
}


trait SpinalTagReady {
  var _spinalTags : mutable.Set[SpinalTag] =  null
  def spinalTags : mutable.Set[SpinalTag] = {
    if(_spinalTags == null)
      _spinalTags = new mutable.HashSet[SpinalTag]{
        override def initialSize: Int = 4
      }
    _spinalTags
  }

  def addTag(spinalTag: SpinalTag): this.type = {
    spinalTags += spinalTag
    this
  }
  def addTags(tags: Iterable[SpinalTag]): this.type = {
    spinalTags ++= tags
    this
  }
  def removeTag(spinalTag: SpinalTag): this.type = {
    if(_spinalTags != null)
      _spinalTags -= spinalTag
    this
  }

  def removeTags(tags: Iterable[SpinalTag]): this.type = {
    if(_spinalTags != null)
      _spinalTags --= tags
    this
  }
  def hasTag(spinalTag: SpinalTag): Boolean = {
    if(_spinalTags == null) return false
    if (_spinalTags.contains(spinalTag)) return true
    return false
  }

  //Feed it with classOf[?] to avoid intermodule problems
  def getTag[T <: SpinalTag](clazz : Class[T]) : Option[T] = {
    if(_spinalTags == null) return None
    val tag = _spinalTags.find(_.getClass == clazz)
    if(tag.isDefined) return Option(tag.get.asInstanceOf[T])
    None
  }
  def findTag(cond : (SpinalTag) => Boolean) : Option[SpinalTag] = {
    if(_spinalTags == null) return None
    _spinalTags.find(cond)
  }
  def existsTag(cond : (SpinalTag) => Boolean) : Boolean = {
    if(_spinalTags == null) return false
    _spinalTags.exists(cond)
  }
  def isEmptyOfTag : Boolean = {
    if(_spinalTags == null) return true
    _spinalTags.isEmpty
  }
  def filterTag(cond : (SpinalTag) => Boolean) : Iterable[SpinalTag] = {
    if(_spinalTags == null) return Nil
    _spinalTags.filter(cond)
  }

  def addAttribute(attribute: Attribute): this.type
  def addAttribute(name: String): this.type = addAttribute(new AttributeFlag(name))
  def addAttribute(name: String,value : String): this.type = addAttribute(new AttributeString(name,value))
  def onEachAttributes(doIt : (Attribute) => Unit) : Unit = {
    if(_spinalTags == null) return
    _spinalTags.foreach(_ match{
      case attribute : Attribute => doIt(attribute)
      case _ =>
    })
  }

  def instanceAttributes : Iterable[Attribute] = {
    if(_spinalTags == null) return Nil
    val array = ArrayBuffer[Attribute]()
    _spinalTags.foreach(e => if(e.isInstanceOf[Attribute])array += e.asInstanceOf[Attribute])
    array
  }

  def instanceAttributes(language: Language) : Iterable[Attribute] = {
    if(_spinalTags == null) return Nil
    val array = ArrayBuffer[Attribute]()
    _spinalTags.foreach(e => if(e.isInstanceOf[Attribute] && e.asInstanceOf[Attribute].isLanguageReady(language))array += e.asInstanceOf[Attribute])
    array
  }
}

object SpinalTagReady{
  def splitNewSink(source : SpinalTagReady,sink : SpinalTagReady) : Unit = {
    source.instanceAttributes.foreach(e => {
      if(e.duplicative){
        sink.addTag(e)
      }
    })
  }
}

trait SpinalTag {
  def isAssignedTo(that: SpinalTagReady) = that.hasTag(this)
  def moveToSyncNode = false //When true, Spinal will automaticaly move the tag to the driving syncNode
  def duplicative = false
  def driverShouldNotChange = false
  def canSymplifyHost = false
}

object unusedTag extends SpinalTag
object crossClockDomain extends SpinalTag{override def moveToSyncNode = true}
object crossClockBuffer extends SpinalTag{override def moveToSyncNode = true}
object randomBoot extends SpinalTag{override def moveToSyncNode = true}
object tagAutoResize extends SpinalTag{
  override def duplicative = true
  override def canSymplifyHost: Boolean = true
}
object tagTruncated extends SpinalTag{
  override def duplicative = true
  override def canSymplifyHost: Boolean = true
}

trait Area extends Nameable with ContextUser with OwnableRef with ScalaLocated{
  component.addPrePopTask(reflectNames)

  def reflectNames() : Unit = {
    Misc.reflect(this, (name, obj) => {
      obj match {
        case component: Component => {
          if (component.parent == this.component) {
            component.setPartialName(name,true)
            OwnableRef.proposal(component,this)
          }
        }
        case namable: Nameable => {
          if (!namable.isInstanceOf[ContextUser]) {
            namable.setPartialName(name,true)
            OwnableRef.proposal(namable,this)
          } else if (namable.asInstanceOf[ContextUser].component == component){
            namable.setPartialName(name,true)
            OwnableRef.proposal(namable,this)
          } else {
            for (kind <- component.children) {
              //Allow to name a component by his io reference into the parent component
              if (kind.reflectIo == namable) {
                kind.setPartialName(name,true)
                OwnableRef.proposal(kind,this)
              }
            }
          }
        }
        case _ =>
      }
    })
  }


  //  def keepAll() : Unit = {
  //    Misc.reflect(this, (name, obj) => {
  //      obj match {
  //        case data : Data => data.keep()
  //        case area : Area => area.keepAll()
  //      }
  //    }
  //  }
  override def toString(): String = component.getPath() + "/" + super.toString()
}

object ImplicitArea{
  implicit def toImplicit[T](area: ImplicitArea[T]): T = area.implicitValue
}
abstract class ImplicitArea[T] extends Area {
  def implicitValue: T
}

class ClockingArea(clockDomain: ClockDomain) extends Area with DelayedInit {
  clockDomain.push

  override def delayedInit(body: => Unit) = {
    body

    if ((body _).getClass.getDeclaringClass == this.getClass) {
      clockDomain.pop
    }
  }
}

class ClockEnableArea(clockEnable: Bool) extends Area with DelayedInit {
  val newClockEnable : Bool = if (ClockDomain.current.config.clockEnableActiveLevel == HIGH)
    ClockDomain.current.readClockEnableWire & clockEnable
  else
    ClockDomain.current.readClockEnableWire | !clockEnable

  val clockDomain = ClockDomain.current.clone(clockEnable = newClockEnable)

  clockDomain.push

  override def delayedInit(body: => Unit) = {
    body

    if ((body _).getClass.getDeclaringClass == this.getClass) {
      clockDomain.pop
    }
  }
}

class SlowArea(factor : BigInt) extends ClockingArea(ClockDomain.current.newClockDomainSlowedBy(factor)){
  def this(frequency : HertzNumber)  {
    this((ClockDomain.current.frequency.getValue / frequency).toBigInt())
    val factor = ClockDomain.current.frequency.getValue / frequency
    require(factor.toBigInt() == factor)
  }
}



class ResetArea(reset: Bool, cumulative: Boolean) extends Area with DelayedInit {

  val newReset : Bool = if (ClockDomain.current.config.resetActiveLevel == HIGH)
    (if (cumulative) (ClockDomain.current.readResetWire & reset) else reset)
  else
    (if (cumulative) (ClockDomain.current.readResetWire | !reset) else reset)
  val clockDomain = ClockDomain.current.clone(reset = newReset)
  clockDomain.push

  override def delayedInit(body: => Unit) = {
    body

    if ((body _).getClass.getDeclaringClass == this.getClass) {
      clockDomain.pop
    }
  }
}



object GlobalData {

  /** Provide a thread local variable (Create a GlobalData for each thread) */
  private val it = new ThreadLocal[GlobalData]

  /** Return the GlobalData of the current thread */
  def get = it.get()

  /** Reset the GlobalData of the current thread */
  def reset = {
    it.set(new GlobalData)
    get
  }
}


/**
  *
  */
class GlobalData {

  var algoId = 1

  def allocateAlgoId(): Int = {
    algoId += 1
    return algoId - 1
  }

  var anonymSignalPrefix : String = null
  var commonClockConfig = ClockDomainConfig()
  var overridingAssignementWarnings = true
  var nodeAreNamed = false
  var nodeAreInferringWidth = false
  var nodeAreInferringEnumEncoding = false
  val nodeGetWidthWalkedSet = mutable.Set[Widthable]()
  val clockSyncronous = mutable.HashMap[Bool,ArrayBuffer[Bool]]()
  /** Stack to store all clocDomain */
  val clockDomainStack = new SafeStack[ClockDomain]
  /** Stack to store all components */
  val componentStack = new SafeStackWithStackable[Component]
  val switchStack = new SafeStack[SwitchStack]
  val conditionalAssignStack = new SafeStack[ConditionalContext]
  var scalaLocatedEnable = false
  var instanceCounter = 0
  val pendingErrors = mutable.ArrayBuffer[() => String]()
  val postBackendTask = mutable.ArrayBuffer[() => Unit]()
  var netlistLockError = new Stack[() => Unit]()

  netlistLockError.push(null)

  def pushNetlistLock(error: () => Unit): Unit = netlistLockError.push(error)

  def popNetlistLock(): Unit = netlistLockError.pop

  def pushNetlistUnlock(): Unit = netlistLockError.push(null)

  def popNetlistUnlock(): Unit = netlistLockError.pop

  def netlistUpdate(): Unit = {
    if(netlistLockError.head != null){
      netlistLockError.head()
    }
  }

  val jsonReports = ArrayBuffer[String]()

  def getInstanceCounter: Int = {
    val temp = instanceCounter
    instanceCounter = instanceCounter + 1
    temp
  }

  def getThrowable() = if(scalaLocatedEnable) new Throwable else null

  def addPostBackendTask(task: => Unit): Unit = postBackendTask += (() => task)
  def addJsonReport(report: String): Unit = jsonReports += report
}


trait OverridedEqualsHashCode{
  override def equals(obj: scala.Any): Boolean = super.equals(obj)
  override def hashCode(): Int = super.hashCode()
}


/**
  * Base operations for numbers
  * @tparam T the type which is associated with the base operation
  */
trait Num[T <: Data] {

  /** Addition */
  def +  (right: T): T
  /** Substraction */
  def -  (right: T): T
  /** Multiplication */
  def *  (right: T): T
  /** Division */
  def /  (right: T): T
  /** Modulo */
  def %  (right: T): T

  /** Is less than right */
  def <  (right: T): Bool
  /** Is equal or less than right */
  def <= (right: T): Bool
  /** Is greater than right */
  def >  (right: T): Bool
  /** Is equal or greater than right */
  def >= (right: T): Bool

  /** Logical left shift (w(T) = w(this) + shift)*/
  def << (shift : Int) : T
  /** Logical right shift (w(T) = w(this) - shift)*/
  def >> (shift : Int) : T

  /** Return the minimum value between this and right  */
  def min(right: T): T = Mux(this < right, this.asInstanceOf[T], right)
  /** Return the maximum value between this and right  */
  def max(right: T): T = Mux(this < right, right, this.asInstanceOf[T])
}


/**
  * Bitwise Operation
  * @tparam T the type which is associated with the bitwise operation
  */
trait BitwiseOp[T <: Data]{

  /** Logical AND operator */
  def &(right: T): T

  /** Logical OR operator */
  def |(right: T): T

  /** Logical XOR operator */
  def ^(right: T): T

  /** Inverse bitwise operator */
  def unary_~ : T
}