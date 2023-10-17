package io.github.teonistor.scalamacros

class More {
/* More macro ideas

    @JsonEnumerated(Map[element:T -> name:String]) or @JsonEnumerated(Seq[element:T])
      Will have to work with element [1] (the companion object) or create it if not given, and create two classes within it
      extending JsonSerializer and JsonDeserializer respectively. The logic would be a specialised version of the logic in
      EnumeratedTypeModule. Also generate @JsonSerialize and @JsonDeserialize onto element [0]
	  
	Annotation-heavy fixed width serialisation
	  - Probably useless as there are better tools in the world (at best, not entirely useless if used in conjunction with those to augment)
	  - Not sure if can be shafted into something like Jackson (it's not a special JSON object, it's a completely new kind of serialisation)
	  Some top-of-the-brain thoughts:
	    @FixedWidthSerialisation - macro entry point to be put on class
	    @Field(position:Int, width:Int, paddingSide:SIDE default RIGHT, paddingCharacter:Char default ' ', tooLongAction:ACTION default ERROR)
		@NumericField(position:Int, width:Int) - as above but defaults to padding left with '0'
		@Serialize / @Deserialize - for nonprimitives/not-default-supported types which aren't themselves marked @FixedWidthSerialisation. Take in method/function names, maybe not even as strings if we can persuade the macro intervention to get over the apparent initial type error
 */
}
