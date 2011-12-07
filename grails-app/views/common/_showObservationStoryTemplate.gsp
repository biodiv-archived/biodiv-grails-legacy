
<div class="grid_5" class="observation_story sidebar">
	<div class="rating" style="float: right">
		<input class="star" type="radio" name="orating" value="1"
			title="Worst" /> <input class="star" type="radio" name="orating"
			value="2" title="Bad" /> <input class="star" type="radio"
			name="orating" value="3" title="OK" /> <input class="star"
			type="radio" name="orating" value="4" title="Good" /> <input
			class="star" type="radio" name="orating" value="5" title="Best" />
	</div>
	<div>
		<p class="prop">
			<span class="name">By </span>
			<g:link controller="sUser" action="show"
				id="${observationInstance.author.id}">
				${observationInstance.author.username}
			</g:link>
		</p>
	</div>
	<div>
		<p class="prop">
			<span class="name">Observed on</span> <span class="value"><g:formatDate
					format="MMMMM dd, yyyy" date="${observationInstance.observedOn}" />
			</span>
		</p>
	</div>

	<div class="readmore">
		<p class="prop">
			<span class="name">Description </span> <span class="value"> ${observationInstance.notes}
			</span>
		</p>
	</div>
</div>