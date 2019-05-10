package de.kah2.mondtag.calendar;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import de.kah2.mondtag.R;

/** {@link android.support.v7.widget.RecyclerView.Adapter} to show all
 *  {@link de.kah2.libZodiac.interpretation.Interpreter}s in {@link DayDetailFragment}.
 */
public class DayDetailInterpretationListAdapter
        extends RecyclerView.Adapter<DayDetailInterpretationListAdapter.InterpretationListItem> {

    private final LinkedList<MappedInterpreter> interpreters;

    DayDetailInterpretationListAdapter(LinkedList<MappedInterpreter> interpreters) {
        this.interpreters = interpreters;
    }

    @NonNull
    @Override
    public InterpretationListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( R.layout.fragment_day_detail_interpretation_item,
                        parent,
                        false);

        return new InterpretationListItem(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull InterpretationListItem holder, int position) {

        final MappedInterpreter interpreter = interpreters.get(position);
        holder.bindElement(interpreter);
    }

    @Override
    public int getItemCount() {

        return interpreters.size();
    }

    class InterpretationListItem extends RecyclerView.ViewHolder {

        InterpretationListItem(View itemView) {
            super(itemView);
        }

        void bindElement(MappedInterpreter interpreter) {
            ( (ImageView) itemView.findViewById(R.id.interpretation_icon) )
                    .setImageResource( interpreter.getQualityIcon() );
            ( (TextView) itemView.findViewById(R.id.interpretation_name) )
                    .setText( interpreter.getId() );
            ( (TextView) itemView.findViewById(R.id.interpretation_quality_text) )
                    .setText( interpreter.getQualityText() );
            ( (TextView) itemView.findViewById(R.id.interpretation_annotation_text) )
                    .setText( interpreter.getAnnotations() );
        }
    }
}
